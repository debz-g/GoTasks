package com.debzg.gotasks.domain.repository

import com.debzg.gotasks.domain.model.Task
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
  fun observeTasks(taskListId: String): Flow<List<Task>>

  suspend fun createTask(
    taskListId: String,
    title: String,
    notes: String? = null,
    parentId: String? = null,
    isStarred: Boolean = false,
    due: Instant? = null,
  ): String

  /** [due] is the full intended state: null clears any existing due date. */
  suspend fun updateTask(
    taskId: String,
    title: String,
    notes: String?,
    isStarred: Boolean,
    due: Instant?,
    hasTime: Boolean,
  )

  suspend fun setCompleted(taskId: String, isCompleted: Boolean)

  suspend fun deleteTask(taskId: String)
}
