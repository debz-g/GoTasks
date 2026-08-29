package com.debzg.gotasks.presentation.tasks

import com.debzg.gotasks.domain.model.Task

sealed interface TasksIntent {
  data object Refresh : TasksIntent

  data object ToggleCompletedSection : TasksIntent

  data object ToggleListSwitcher : TasksIntent

  data class SelectTaskList(val taskListId: String) : TasksIntent

  data class ToggleTaskCompleted(val task: Task) : TasksIntent

  data object ShowAddTaskDialog : TasksIntent

  data class ShowEditTaskDialog(val task: Task) : TasksIntent

  data object ShowNewListDialog : TasksIntent

  data object ShowRenameListDialog : TasksIntent

  data object ShowDeleteListConfirmDialog : TasksIntent

  data object DismissDialog : TasksIntent

  data class SubmitAddTask(val title: String, val notes: String?, val isStarred: Boolean) : TasksIntent

  data class SubmitEditTask(val taskId: String, val title: String, val notes: String?, val isStarred: Boolean) : TasksIntent

  data class SubmitDeleteTask(val taskId: String) : TasksIntent

  data class SubmitNewList(val title: String) : TasksIntent

  data class SubmitRenameList(val title: String) : TasksIntent

  data object SubmitDeleteList : TasksIntent
}
