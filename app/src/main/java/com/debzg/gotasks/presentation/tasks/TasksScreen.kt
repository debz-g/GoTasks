package com.debzg.gotasks.presentation.tasks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.debzg.gotasks.presentation.common.components.CircularFab
import com.debzg.gotasks.presentation.common.components.CompletedSection
import com.debzg.gotasks.presentation.common.components.TaskRow
import com.debzg.gotasks.presentation.listmanagement.DeleteListConfirmDialog
import com.debzg.gotasks.presentation.listmanagement.NewListDialog
import com.debzg.gotasks.presentation.listmanagement.RenameListDialog
import com.debzg.gotasks.ui.theme.AccentCoral
import com.debzg.gotasks.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel

@Composable
fun TasksScreen(modifier: Modifier = Modifier, viewModel: TasksViewModel = koinViewModel()) {
  val state by viewModel.state.collectAsState()

  Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      TasksTopBar(state = state, onIntent = viewModel::onIntent)

      when {
        state.isLoading && state.activeTasks.isEmpty() && state.completedTasks.isEmpty() -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AccentCoral) }
        }

        state.errorMessage != null && state.activeTasks.isEmpty() && state.completedTasks.isEmpty() -> {
          Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(text = state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
          }
        }

        state.activeTasks.isEmpty() && state.completedTasks.isEmpty() -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No Task", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
          }
        }

        else -> {
          LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.activeTasks, key = { it.id }) { task ->
              TaskRow(
                task = task,
                onToggleCompleted = { viewModel.onIntent(TasksIntent.ToggleTaskCompleted(task)) },
                onClick = { viewModel.onIntent(TasksIntent.ShowEditTaskDialog(task)) },
              )
            }
            item {
              CompletedSection(
                completedTasks = state.completedTasks,
                isExpanded = state.isCompletedSectionExpanded,
                onToggle = { viewModel.onIntent(TasksIntent.ToggleCompletedSection) },
                onToggleTaskCompleted = { task -> viewModel.onIntent(TasksIntent.ToggleTaskCompleted(task)) },
                onTaskClick = { task -> viewModel.onIntent(TasksIntent.ShowEditTaskDialog(task)) },
              )
            }
          }
        }
      }
    }

    CircularFab(
      onClick = { viewModel.onIntent(TasksIntent.ShowAddTaskDialog) },
      modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
    )
  }

  when (val dialog = state.dialog) {
    is TasksDialog.AddTask ->
      AddTaskSheet(
        listTitle = state.listTitle,
        onConfirm = { title, notes, isStarred -> viewModel.onIntent(TasksIntent.SubmitAddTask(title, notes, isStarred)) },
        onDismiss = { viewModel.onIntent(TasksIntent.DismissDialog) },
      )

    is TasksDialog.EditTask ->
      EditTaskSheet(
        task = dialog.task,
        listTitle = state.listTitle,
        onSave = { title, notes, isStarred -> viewModel.onIntent(TasksIntent.SubmitEditTask(dialog.task.id, title, notes, isStarred)) },
        onDelete = { viewModel.onIntent(TasksIntent.SubmitDeleteTask(dialog.task.id)) },
        onToggleCompleted = { viewModel.onIntent(TasksIntent.ToggleTaskCompleted(dialog.task)) },
        onDismiss = { viewModel.onIntent(TasksIntent.DismissDialog) },
      )

    is TasksDialog.NewList ->
      NewListDialog(onConfirm = { viewModel.onIntent(TasksIntent.SubmitNewList(it)) }, onDismiss = { viewModel.onIntent(TasksIntent.DismissDialog) })

    is TasksDialog.RenameList ->
      RenameListDialog(
        currentTitle = state.listTitle,
        onConfirm = { viewModel.onIntent(TasksIntent.SubmitRenameList(it)) },
        onDismiss = { viewModel.onIntent(TasksIntent.DismissDialog) },
      )

    is TasksDialog.DeleteListConfirm ->
      DeleteListConfirmDialog(
        listTitle = state.listTitle,
        onConfirm = { viewModel.onIntent(TasksIntent.SubmitDeleteList) },
        onDismiss = { viewModel.onIntent(TasksIntent.DismissDialog) },
      )

    null -> Unit
  }
}
