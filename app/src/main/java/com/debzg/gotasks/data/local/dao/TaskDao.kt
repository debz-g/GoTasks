package com.debzg.gotasks.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.debzg.gotasks.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
  @Query("SELECT * FROM tasks WHERE taskListId = :taskListId AND deleted = 0 ORDER BY position")
  fun observeTasksForList(taskListId: String): Flow<List<TaskEntity>>

  @Query("SELECT * FROM tasks WHERE id = :id") suspend fun getById(id: String): TaskEntity?

  @Query("DELETE FROM tasks WHERE id = :id") suspend fun deleteById(id: String)

  /** Re-points subtasks whose parent was still a temporary local id when they were created. */
  @Query("UPDATE tasks SET parent = :newId WHERE parent = :oldId") suspend fun reparentChildren(oldId: String, newId: String)

  @Upsert suspend fun upsertAll(tasks: List<TaskEntity>)
}
