package com.debzg.gotasks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.debzg.gotasks.data.local.entity.PendingOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {
  @Insert suspend fun insert(operation: PendingOperationEntity)

  @Query("SELECT * FROM pending_operations ORDER BY createdAt") suspend fun getAllOrderedByCreatedAt(): List<PendingOperationEntity>

  /**
   * Oldest queued op, re-read fresh each iteration so it picks up id remapping done by an earlier
   * op in the same drain (e.g. a list CREATE reconciling before the tasks queued under it).
   */
  @Query("SELECT * FROM pending_operations WHERE opId NOT IN (:excludedOpIds) ORDER BY createdAt LIMIT 1")
  suspend fun getNextPending(excludedOpIds: List<Long>): PendingOperationEntity?

  @Query("SELECT COUNT(*) FROM pending_operations") fun observeCount(): Flow<Int>

  @Query("SELECT EXISTS(SELECT 1 FROM pending_operations WHERE localEntityId = :entityId)") suspend fun hasPendingOps(entityId: String): Boolean

  @Query("SELECT DISTINCT localEntityId FROM pending_operations") suspend fun getAllPendingEntityIds(): List<String>

  @Query("UPDATE pending_operations SET localEntityId = :newId WHERE localEntityId = :oldId") suspend fun remapEntityId(oldId: String, newId: String)

  @Query("UPDATE pending_operations SET taskListId = :newId WHERE taskListId = :oldId") suspend fun remapTaskListId(oldId: String, newId: String)

  @Query("UPDATE pending_operations SET retryCount = retryCount + 1, lastAttemptAt = :attemptedAt, lastError = :error WHERE opId = :opId")
  suspend fun recordFailure(opId: Long, attemptedAt: Long, error: String?)

  @Query("DELETE FROM pending_operations WHERE taskListId = :taskListId") suspend fun deleteAllForTaskList(taskListId: String)

  @Query("DELETE FROM pending_operations WHERE opId = :opId") suspend fun deleteById(opId: Long)
}
