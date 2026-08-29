package com.debzg.gotasks.presentation.tasks

import com.debzg.gotasks.domain.model.Task
import com.debzg.gotasks.domain.model.TaskList

sealed interface TasksDialog {
  data object AddTask : TasksDialog

  data class EditTask(val task: Task) : TasksDialog

  data object ListPicker : TasksDialog

  data object NewList : TasksDialog

  data object RenameList : TasksDialog

  data object DeleteListConfirm : TasksDialog
}

data class TasksState(
  val isLoading: Boolean = true,
  val taskLists: List<TaskList> = emptyList(),
  val activeTaskListId: String? = null,
  val listTitle: String = "",
  val activeTasks: List<Task> = emptyList(),
  val completedTasks: List<Task> = emptyList(),
  val isCompletedSectionExpanded: Boolean = false,
  val dialog: TasksDialog? = null,
  val errorMessage: String? = null,
)
