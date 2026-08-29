package com.debzg.gotasks.domain.repository

import com.debzg.gotasks.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
  fun observeTasks(taskListId: String): Flow<List<Task>>

  suspend fun createTask(taskListId: String, title: String, notes: String? = null, parentId: String? = null, isStarred: Boolean = false): String

  suspend fun updateTask(taskId: String, title: String, notes: String?, isStarred: Boolean)

  suspend fun setCompleted(taskId: String, isCompleted: Boolean)

  suspend fun deleteTask(taskId: String)
}
