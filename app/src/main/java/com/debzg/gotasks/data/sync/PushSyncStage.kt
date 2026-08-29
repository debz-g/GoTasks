package com.debzg.gotasks.data.sync

import android.util.Log
import androidx.room.withTransaction
import com.debzg.gotasks.data.auth.AuthState
import com.debzg.gotasks.data.auth.AuthStateRepository
import com.debzg.gotasks.data.local.AppDatabase
import com.debzg.gotasks.data.local.dao.PendingOperationDao
import com.debzg.gotasks.data.local.dao.TaskDao
import com.debzg.gotasks.data.local.dao.TaskListDao
import com.debzg.gotasks.data.local.entity.EntityType
import com.debzg.gotasks.data.local.entity.OperationType
import com.debzg.gotasks.data.local.entity.PendingOperationEntity
import com.debzg.gotasks.data.mapper.isLocalId
import com.debzg.gotasks.data.mapper.toEntity
import com.debzg.gotasks.data.remote.TasksApiService
import com.debzg.gotasks.data.remote.dto.TaskDto
import com.debzg.gotasks.data.remote.dto.TaskListDto
import com.debzg.gotasks.data.remote.dto.TaskUpdateDto
import java.io.IOException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

/** How many times a single op may fail with a non-retryable error before it's dropped. */
private const val MAX_RETRIES = 3

/**
 * Drains the outbox against the real Tasks API, oldest op first and strictly one at a time.
 *
 * The serial ordering is what makes temp-id reconciliation safe: a CREATE always completes and
 * remaps its local id before any later op that references the same entity is attempted.
 */
class PushSyncStage(
  private val database: AppDatabase,
  private val taskDao: TaskDao,
  private val taskListDao: TaskListDao,
  private val pendingOperationDao: PendingOperationDao,
  private val api: TasksApiService,
  private val json: Json,
  private val authStateRepository: AuthStateRepository,
) {

  suspend fun push(): SyncOutcome {
    val skippedOpIds = mutableListOf<Long>()

    while (true) {
      // Re-read each iteration so id remapping from earlier ops in this same drain is picked up.
      val op = pendingOperationDao.getNextPending(skippedOpIds.ifEmpty { listOf(-1L) }) ?: return SyncOutcome.Success

      try {
        when (op.entityType) {
          EntityType.TASK -> pushTaskOperation(op)
          EntityType.TASK_LIST -> pushTaskListOperation(op)
        }
        pendingOperationDao.deleteById(op.opId)
      } catch (e: HttpException) {
        when {
          e.code() == 401 || e.code() == 403 -> {
            // The OkHttp authenticator already tried a silent refresh and gave up.
            Log.w(TAG, "Authorization required while pushing op ${op.opId}", e)
            authStateRepository.update(AuthState.NeedsReauthorization)
            return SyncOutcome.AuthRequired
          }
          e.code() == 404 && op.operationType == OperationType.DELETE -> {
            // Already gone server-side — the desired end state, so treat as success.
            pendingOperationDao.deleteById(op.opId)
          }
          e.code() in 400..499 -> {
            Log.w(TAG, "Non-retryable error on op ${op.opId} (HTTP ${e.code()})", e)
            failOp(op, skippedOpIds, "HTTP ${e.code()}: ${e.message()}")
          }
          else -> {
            Log.w(TAG, "Server error on op ${op.opId} (HTTP ${e.code()}); will retry later", e)
            failOp(op, skippedOpIds, "HTTP ${e.code()}")
            return SyncOutcome.Retry
          }
        }
      } catch (e: IOException) {
        // Network is down — no point walking the rest of the queue this run.
        Log.w(TAG, "Network error pushing op ${op.opId}; will retry later", e)
        failOp(op, skippedOpIds, e.message)
        return SyncOutcome.Retry
      }
    }
  }

  private suspend fun failOp(op: PendingOperationEntity, skippedOpIds: MutableList<Long>, error: String?) {
    pendingOperationDao.recordFailure(op.opId, System.currentTimeMillis(), error)
    if (op.retryCount + 1 >= MAX_RETRIES) {
      Log.w(TAG, "Dropping op ${op.opId} after ${op.retryCount + 1} failed attempts: $error")
      pendingOperationDao.deleteById(op.opId)
    } else {
      skippedOpIds += op.opId
    }
  }

  private suspend fun pushTaskOperation(op: PendingOperationEntity) {
    when (op.operationType) {
      OperationType.CREATE -> {
        val payload = decodeTask(op)
        val created = api.createTask(taskListId = op.taskListId, body = payload.copy(id = null), parent = payload.parent)
        reconcileTaskId(tempId = op.localEntityId, serverTask = created, taskListId = op.taskListId)
      }

      OperationType.UPDATE,
      OperationType.MOVE -> {
        // A task created locally but not yet pushed has no server id to PATCH; its pending CREATE
        // carries the latest local state anyway, so this op is a no-op.
        if (op.localEntityId.isLocalId()) return
        val updated = api.updateTask(taskListId = op.taskListId, taskId = op.localEntityId, body = decodeTaskUpdate(op))
        upsertServerTask(updated, op.taskListId)
      }

      OperationType.DELETE -> {
        if (op.localEntityId.isLocalId()) return
        api.deleteTask(taskListId = op.taskListId, taskId = op.localEntityId)
        taskDao.deleteById(op.localEntityId)
      }
    }
  }

  private suspend fun pushTaskListOperation(op: PendingOperationEntity) {
    when (op.operationType) {
      OperationType.CREATE -> {
        val payload = json.decodeFromString<TaskListDto>(op.payloadJson)
        val created = api.createTaskList(payload.copy(id = null))
        reconcileTaskListId(tempId = op.localEntityId, serverList = created)
      }

      OperationType.UPDATE -> {
        if (op.localEntityId.isLocalId()) return
        val payload = json.decodeFromString<TaskListDto>(op.payloadJson)
        val updated = api.updateTaskList(taskListId = op.localEntityId, body = payload)
        taskListDao.upsertAll(listOf(updated.toEntity()))
      }

      OperationType.DELETE -> {
        if (op.localEntityId.isLocalId()) return
        api.deleteTaskList(taskListId = op.localEntityId)
        taskListDao.deleteById(op.localEntityId)
      }

      OperationType.MOVE -> Unit // Lists have no ordering in the Tasks API.
    }
  }

  private fun decodeTask(op: PendingOperationEntity): TaskDto = json.decodeFromString<TaskDto>(op.payloadJson)

  private fun decodeTaskUpdate(op: PendingOperationEntity): TaskUpdateDto = json.decodeFromString<TaskUpdateDto>(op.payloadJson)

  private suspend fun upsertServerTask(dto: TaskDto, taskListId: String) {
    taskDao.upsertAll(listOf(dto.toEntity(taskListId, cached = dto.id?.let { taskDao.getById(it) })))
  }

  /** Swaps the temporary local task id for the server's, keeping subtasks and queued ops pointed at it. */
  private suspend fun reconcileTaskId(tempId: String, serverTask: TaskDto, taskListId: String) {
    val serverId = requireNotNull(serverTask.id) { "Created task missing id" }
    val cached = taskDao.getById(tempId)

    database.withTransaction {
      taskDao.deleteById(tempId)
      taskDao.upsertAll(listOf(serverTask.toEntity(taskListId, cached = cached)))
      taskDao.reparentChildren(oldId = tempId, newId = serverId)
      pendingOperationDao.remapEntityId(oldId = tempId, newId = serverId)
    }
  }

  /** Same for lists — the id UPDATE cascades to every task's taskListId via the foreign key. */
  private suspend fun reconcileTaskListId(tempId: String, serverList: TaskListDto) {
    val serverId = requireNotNull(serverList.id) { "Created task list missing id" }

    database.withTransaction {
      taskListDao.updateId(oldId = tempId, newId = serverId)
      taskListDao.upsertAll(listOf(serverList.toEntity()))
      pendingOperationDao.remapEntityId(oldId = tempId, newId = serverId)
      pendingOperationDao.remapTaskListId(oldId = tempId, newId = serverId)
    }
  }

  private companion object {
    const val TAG = "PushSyncStage"
  }
}
