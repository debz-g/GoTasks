package com.debzg.gotasks.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debzg.gotasks.domain.repository.TaskListRepository
import com.debzg.gotasks.domain.repository.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TasksViewModel(private val taskListRepository: TaskListRepository, private val taskRepository: TaskRepository) : ViewModel() {

  private val _state = MutableStateFlow(TasksState())
  val state: StateFlow<TasksState> = _state.asStateFlow()

  private var tasksObservationJob: Job? = null

  init {
    observeTaskLists()
    viewModelScope.launch {
      try {
        taskListRepository.refreshTaskLists()
      } catch (e: Exception) {
        _state.update { it.copy(errorMessage = e.message ?: "Couldn't load lists") }
      }
    }
  }

  fun onIntent(intent: TasksIntent) {
    when (intent) {
      is TasksIntent.Refresh -> viewModelScope.launch { refresh() }
      is TasksIntent.ToggleCompletedSection -> _state.update { it.copy(isCompletedSectionExpanded = !it.isCompletedSectionExpanded) }
      is TasksIntent.ToggleListSwitcher -> _state.update { it.copy(isListSwitcherExpanded = !it.isListSwitcherExpanded) }
      is TasksIntent.SelectTaskList -> selectTaskList(intent.taskListId, pull = true)
      is TasksIntent.ToggleTaskCompleted -> viewModelScope.launch { taskRepository.setCompleted(intent.task.id, !intent.task.isCompleted) }
      is TasksIntent.ShowAddTaskDialog -> _state.update { it.copy(dialog = TasksDialog.AddTask) }
      is TasksIntent.ShowEditTaskDialog -> _state.update { it.copy(dialog = TasksDialog.EditTask(intent.task)) }
      is TasksIntent.ShowNewListDialog -> _state.update { it.copy(dialog = TasksDialog.NewList, isListSwitcherExpanded = false) }
      is TasksIntent.ShowRenameListDialog -> _state.update { it.copy(dialog = TasksDialog.RenameList) }
      is TasksIntent.ShowDeleteListConfirmDialog -> _state.update { it.copy(dialog = TasksDialog.DeleteListConfirm) }
      is TasksIntent.DismissDialog -> _state.update { it.copy(dialog = null) }
      is TasksIntent.SubmitAddTask -> addTask(intent.title, intent.notes, intent.isStarred)
      is TasksIntent.SubmitEditTask -> editTask(intent.taskId, intent.title, intent.notes, intent.isStarred)
      is TasksIntent.SubmitDeleteTask -> deleteTask(intent.taskId)
      is TasksIntent.SubmitNewList -> createList(intent.title)
      is TasksIntent.SubmitRenameList -> renameList(intent.title)
      is TasksIntent.SubmitDeleteList -> deleteActiveList()
    }
  }

  private fun observeTaskLists() {
    viewModelScope.launch {
      taskListRepository.observeTaskLists().collect { lists ->
        val previousActiveId = _state.value.activeTaskListId
        _state.update { it.copy(taskLists = lists) }

        when {
          previousActiveId == null && lists.isNotEmpty() -> selectTaskList(lists.first().id, pull = true)
          previousActiveId != null && lists.none { it.id == previousActiveId } ->
            lists.firstOrNull()?.let { selectTaskList(it.id, pull = false) } ?: _state.update { it.copy(isLoading = false) }
          else -> lists.find { it.id == previousActiveId }?.let { active -> _state.update { it.copy(listTitle = active.title) } }
        }
      }
    }
  }

  private fun selectTaskList(taskListId: String, pull: Boolean) {
    tasksObservationJob?.cancel()
    val title = _state.value.taskLists.find { it.id == taskListId }?.title.orEmpty()
    _state.update {
      it.copy(
        activeTaskListId = taskListId,
        listTitle = title,
        isListSwitcherExpanded = false,
        activeTasks = emptyList(),
        completedTasks = emptyList(),
      )
    }

    tasksObservationJob =
      viewModelScope.launch {
        taskRepository.observeTasks(taskListId).collect { tasks ->
          _state.update {
            it.copy(
              activeTasks = tasks.filter { task -> !task.isCompleted }.sortedBy { task -> task.position },
              completedTasks = tasks.filter { task -> task.isCompleted }.sortedByDescending { task -> task.completedAt },
            )
          }
        }
      }

    if (pull) {
      viewModelScope.launch {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        try {
          taskRepository.refreshTasks(taskListId)
        } catch (e: Exception) {
          _state.update { it.copy(errorMessage = e.message ?: "Failed to load tasks") }
        }
        _state.update { it.copy(isLoading = false) }
      }
    }
  }

  private suspend fun refresh() {
    val taskListId = _state.value.activeTaskListId ?: return
    _state.update { it.copy(isLoading = true, errorMessage = null) }
    try {
      taskListRepository.refreshTaskLists()
      taskRepository.refreshTasks(taskListId)
    } catch (e: Exception) {
      _state.update { it.copy(errorMessage = e.message ?: "Refresh failed") }
    }
    _state.update { it.copy(isLoading = false) }
  }

  private fun addTask(title: String, notes: String?, isStarred: Boolean) {
    val listId = _state.value.activeTaskListId
    if (listId == null || title.isBlank()) {
      _state.update { it.copy(dialog = null) }
      return
    }
    viewModelScope.launch {
      taskRepository.createTask(taskListId = listId, title = title.trim(), notes = notes?.trim()?.ifBlank { null }, isStarred = isStarred)
      _state.update { it.copy(dialog = null) }
    }
  }

  private fun editTask(taskId: String, title: String, notes: String?, isStarred: Boolean) {
    if (title.isBlank()) {
      _state.update { it.copy(dialog = null) }
      return
    }
    viewModelScope.launch {
      taskRepository.updateTask(taskId = taskId, title = title.trim(), notes = notes?.trim()?.ifBlank { null }, isStarred = isStarred)
      _state.update { it.copy(dialog = null) }
    }
  }

  private fun deleteTask(taskId: String) {
    viewModelScope.launch {
      taskRepository.deleteTask(taskId)
      _state.update { it.copy(dialog = null) }
    }
  }

  private fun createList(title: String) {
    if (title.isBlank()) {
      _state.update { it.copy(dialog = null) }
      return
    }
    viewModelScope.launch {
      val newId = taskListRepository.createTaskList(title.trim())
      _state.update { it.copy(dialog = null) }
      selectTaskList(newId, pull = false)
    }
  }

  private fun renameList(title: String) {
    val listId = _state.value.activeTaskListId
    if (listId == null || title.isBlank()) {
      _state.update { it.copy(dialog = null) }
      return
    }
    viewModelScope.launch {
      taskListRepository.renameTaskList(listId, title.trim())
      _state.update { it.copy(dialog = null) }
    }
  }

  private fun deleteActiveList() {
    val listId = _state.value.activeTaskListId ?: return
    viewModelScope.launch {
      taskListRepository.deleteTaskList(listId)
      _state.update { it.copy(dialog = null, activeTaskListId = null) }
    }
  }
}
