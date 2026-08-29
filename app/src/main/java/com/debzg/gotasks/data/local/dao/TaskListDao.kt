package com.debzg.gotasks.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.debzg.gotasks.data.local.entity.TaskListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskListDao {
  @Query("SELECT * FROM task_lists ORDER BY title") fun observeAll(): Flow<List<TaskListEntity>>

  @Query("SELECT * FROM task_lists ORDER BY title") suspend fun getAll(): List<TaskListEntity>

  @Query("SELECT * FROM task_lists WHERE id = :id") suspend fun getById(id: String): TaskListEntity?

  @Query("DELETE FROM task_lists WHERE id = :id") suspend fun deleteById(id: String)

  /** Swaps a temporary local id for the server-assigned one; cascades to tasks.taskListId. */
  @Query("UPDATE task_lists SET id = :newId WHERE id = :oldId") suspend fun updateId(oldId: String, newId: String)

  @Upsert suspend fun upsertAll(taskLists: List<TaskListEntity>)
}
