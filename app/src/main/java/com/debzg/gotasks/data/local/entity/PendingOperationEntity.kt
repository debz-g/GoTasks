package com.debzg.gotasks.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class EntityType {
  TASK,
  TASK_LIST,
}

enum class OperationType {
  CREATE,
  UPDATE,
  DELETE,
}

@Entity(tableName = "pending_operations", indices = [Index("localEntityId"), Index("taskListId")])
data class PendingOperationEntity(
  @PrimaryKey(autoGenerate = true) val opId: Long = 0,
  val entityType: EntityType,
  val operationType: OperationType,
  val localEntityId: String,
  val taskListId: String,
  val payloadJson: String,
  val createdAt: Long,
  val retryCount: Int = 0,
  val lastAttemptAt: Long? = null,
  val lastError: String? = null,
)
