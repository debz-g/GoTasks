package com.debzg.gotasks.data.repository

import com.debzg.gotasks.data.local.OutboxRecorder
import com.debzg.gotasks.data.local.dao.TaskDao
import com.debzg.gotasks.data.local.entity.EntityType
import com.debzg.gotasks.data.local.entity.OperationType
import com.debzg.gotasks.data.local.entity.SyncState
import com.debzg.gotasks.data.local.entity.TaskEntity
import com.debzg.gotasks.data.mapper.LOCAL_ID_PREFIX
import com.debzg.gotasks.data.mapper.toDomain
import com.debzg.gotasks.data.mapper.toEntity
import com.debzg.gotasks.data.remote.dto.TaskDto
import com.debzg.gotasks.data.remote.dto.TaskUpdateDto
import com.debzg.gotasks.domain.model.Task
import com.debzg.gotasks.domain.repository.TaskRepository
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Purely local: reads and optimistic writes against Room, with every mutation queued in the outbox. */
class TaskRepositoryImpl(private val taskDao: TaskDao, private val outboxRecorder: OutboxRecorder) : TaskRepository {

  override fun observeTasks(taskListId: String): Flow<List<Task>> =
    taskDao.observeTasksForList(taskListId).map { entities -> entities.map { it.toDomain() } }

  override suspend fun createTask(
    taskListId: String,
    title: String,
    notes: String?,
    parentId: String?,
    isStarred: Boolean,
    due: Instant?,
  ): String {
    val id = LOCAL_ID_PREFIX + UUID.randomUUID()
    val now = System.currentTimeMillis()
    val entity =
      TaskEntity(
        id = id,
        taskListId = taskListId,
        title = title,
        notes = notes,
        status = "needsAction",
        due = due?.toEpochMilli(),
        completed = null,
        deleted = false,
        hidden = false,
        parent = parentId,
        position = localPosition(now),
        etag = null,
        updated = now,
        selfLink = null,
        webViewLink = null,
        isStarred = isStarred,
        localReminderTime = due?.toEpochMilli(),
        syncState = SyncState.PENDING_CREATE,
      )
    taskDao.upsertAll(listOf(entity))
    outboxRecorder.record(
      EntityType.TASK,
      OperationType.CREATE,
      localEntityId = id,
      taskListId = taskListId,
      payload = TaskDto(title = title, notes = notes, parent = parentId, due = due?.toGoogleDueDate()),
    )
    return id
  }

  override suspend fun updateTask(
    taskId: String,
    title: String,
    notes: String?,
    isStarred: Boolean,
    due: Instant?,
    hasTime: Boolean,
  ) {
    val existing = taskDao.getById(taskId) ?: return
    taskDao.upsertAll(
      listOf(
        existing.copy(
          title = title,
          notes = notes,
          isStarred = isStarred,
          due = due?.toEpochMilli(),
          localReminderTime = if (hasTime) due?.toEpochMilli() else null,
            updated = System.currentTimeMillis(),
          syncState = nextSyncState(existing),
        )
      )
    )
    outboxRecorder.record(
      EntityType.TASK,
      OperationType.UPDATE,
      localEntityId = taskId,
      taskListId = existing.taskListId,
      // The edit sheet shows the complete intended state, so due is always sent explicitly —
      // JsonNull when cleared, which is the only way to actually unset it server-side.
      payload = TaskUpdateDto(title = title, notes = notes, due = TaskUpdateDto.dueOf(due?.toGoogleDueDate())),
    )
  }

  override suspend fun setCompleted(taskId: String, isCompleted: Boolean) {
    val existing = taskDao.getById(taskId) ?: return
    val completedIso = if (isCompleted) Instant.now().toString() else null
    taskDao.upsertAll(
      listOf(
        existing.copy(
          status = if (isCompleted) "completed" else "needsAction",
          completed = completedIso?.let { Instant.parse(it).toEpochMilli() },
          updated = System.currentTimeMillis(),
          syncState = nextSyncState(existing),
        )
      )
    )
    outboxRecorder.record(
      EntityType.TASK,
      OperationType.UPDATE,
      localEntityId = taskId,
      taskListId = existing.taskListId,
      payload = TaskUpdateDto(status = if (isCompleted) "completed" else "needsAction", completed = completedIso),
    )
  }

  override suspend fun deleteTask(taskId: String) {
    val existing = taskDao.getById(taskId) ?: return
    if (existing.syncState == SyncState.PENDING_CREATE) {
      taskDao.deleteById(taskId) // never synced — nothing to tell the server about
      return
    }
    taskDao.upsertAll(listOf(existing.copy(deleted = true, syncState = SyncState.PENDING_DELETE)))
    outboxRecorder.record<Unit>(
      EntityType.TASK,
      OperationType.DELETE,
      localEntityId = taskId,
      taskListId = existing.taskListId,
      payload = null,
    )
  }
}

private fun nextSyncState(existing: TaskEntity): SyncState =
  if (existing.syncState == SyncState.PENDING_CREATE) SyncState.PENDING_CREATE else SyncState.PENDING_UPDATE

private fun localPosition(now: Long): String = now.toString().padStart(20, '0')

/**
 * The Tasks API's `due` field is date-only — it accepts RFC 3339 but ignores the time component,
 * so it's normalised to UTC midnight. Any precise time the user gave lives in `localReminderTime`.
 */
private fun Instant.toGoogleDueDate(): String =
  atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toString()
