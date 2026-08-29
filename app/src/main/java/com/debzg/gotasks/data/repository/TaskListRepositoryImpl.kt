package com.debzg.gotasks.data.repository

import com.debzg.gotasks.data.local.OutboxRecorder
import com.debzg.gotasks.data.local.dao.PendingOperationDao
import com.debzg.gotasks.data.local.dao.TaskListDao
import com.debzg.gotasks.data.local.entity.EntityType
import com.debzg.gotasks.data.local.entity.OperationType
import com.debzg.gotasks.data.local.entity.TaskListEntity
import com.debzg.gotasks.data.mapper.LOCAL_ID_PREFIX
import com.debzg.gotasks.data.mapper.isLocalId
import com.debzg.gotasks.data.mapper.toDomain
import com.debzg.gotasks.data.mapper.toEntity
import com.debzg.gotasks.data.remote.dto.TaskListDto
import com.debzg.gotasks.domain.model.TaskList
import com.debzg.gotasks.domain.repository.TaskListRepository
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Purely local: reads and optimistic writes against Room, with every mutation queued in the outbox. */
class TaskListRepositoryImpl(
  private val taskListDao: TaskListDao,
  private val outboxRecorder: OutboxRecorder,
  private val pendingOperationDao: PendingOperationDao,
) : TaskListRepository {

  override fun observeTaskLists(): Flow<List<TaskList>> = taskListDao.observeAll().map { entities -> entities.map { it.toDomain() } }

  override suspend fun createTaskList(title: String): String {
    val id = LOCAL_ID_PREFIX + UUID.randomUUID()
    taskListDao.upsertAll(listOf(TaskListEntity(id = id, title = title, etag = null, updated = System.currentTimeMillis(), selfLink = null)))
    outboxRecorder.record(EntityType.TASK_LIST, OperationType.CREATE, localEntityId = id, taskListId = id, payload = TaskListDto(title = title))
    return id
  }

  override suspend fun renameTaskList(taskListId: String, title: String) {
    val existing = taskListDao.getById(taskListId) ?: return
    taskListDao.upsertAll(listOf(existing.copy(title = title, updated = System.currentTimeMillis())))
    outboxRecorder.record(
      EntityType.TASK_LIST,
      OperationType.UPDATE,
      localEntityId = taskListId,
      taskListId = taskListId,
      payload = TaskListDto(title = title),
    )
  }

  override suspend fun deleteTaskList(taskListId: String) {
    taskListDao.getById(taskListId) ?: return
    val wasNeverSynced = taskListId.isLocalId()
    // Any queued ops for this list's tasks are moot now — the whole list is going away.
    pendingOperationDao.deleteAllForTaskList(taskListId)
    taskListDao.deleteById(taskListId) // cascades to this list's tasks locally
    if (!wasNeverSynced) {
      outboxRecorder.record<Unit>(
        EntityType.TASK_LIST,
        OperationType.DELETE,
        localEntityId = taskListId,
        taskListId = taskListId,
        payload = null,
      )
    }
  }
}
