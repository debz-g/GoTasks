package com.debzg.gotasks.domain.repository

import com.debzg.gotasks.domain.model.TaskList
import kotlinx.coroutines.flow.Flow

interface TaskListRepository {
  fun observeTaskLists(): Flow<List<TaskList>>

  suspend fun createTaskList(title: String): String

  suspend fun renameTaskList(taskListId: String, title: String)

  suspend fun deleteTaskList(taskListId: String)
}
