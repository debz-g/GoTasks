package com.debzg.gotasks.data.local

import com.debzg.gotasks.data.local.dao.PendingOperationDao
import com.debzg.gotasks.data.local.entity.EntityType
import com.debzg.gotasks.data.local.entity.OperationType
import com.debzg.gotasks.data.local.entity.PendingOperationEntity
import com.debzg.gotasks.data.sync.SyncScheduler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Queues a local mutation for the next push, then nudges the sync worker to drain it. */
class OutboxRecorder(
  @PublishedApi internal val pendingOperationDao: PendingOperationDao,
  @PublishedApi internal val json: Json,
  @PublishedApi internal val syncScheduler: SyncScheduler,
) {
  suspend inline fun <reified T> record(
    entityType: EntityType,
    operationType: OperationType,
    localEntityId: String,
    taskListId: String,
    payload: T?,
  ) {
    pendingOperationDao.insert(
      PendingOperationEntity(
        entityType = entityType,
        operationType = operationType,
        localEntityId = localEntityId,
        taskListId = taskListId,
        payloadJson = payload?.let { json.encodeToString(it) } ?: "",
        createdAt = System.currentTimeMillis(),
      )
    )
    syncScheduler.schedulePush()
  }
}
