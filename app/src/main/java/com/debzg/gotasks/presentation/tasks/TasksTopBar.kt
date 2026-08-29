package com.debzg.gotasks.presentation.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.debzg.gotasks.R
import com.debzg.gotasks.ui.theme.TextPrimary

@Composable
fun TasksTopBar(state: TasksState, onIntent: (TasksIntent) -> Unit, modifier: Modifier = Modifier) {
  var showOverflowMenu by remember { mutableStateOf(false) }

  Row(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
    Row(
      modifier = Modifier.weight(1f).clickable { onIntent(TasksIntent.ShowListPicker) },
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(text = state.listTitle.ifBlank { "GoTasks" }, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
      Icon(
        painter = painterResource(R.drawable.ic_chevron_down),
        contentDescription = "Switch list",
        tint = TextPrimary,
        modifier = Modifier.size(20.dp),
      )
    }

    IconButton(onClick = { onIntent(TasksIntent.Refresh) }) {
      Icon(painter = painterResource(R.drawable.ic_refresh), contentDescription = "Refresh", tint = TextPrimary, modifier = Modifier.size(22.dp))
    }

    Box {
      IconButton(onClick = { showOverflowMenu = true }) {
        Icon(painter = painterResource(R.drawable.ic_more_vert), contentDescription = "More", tint = TextPrimary, modifier = Modifier.size(22.dp))
      }
      DropdownMenu(expanded = showOverflowMenu, onDismissRequest = { showOverflowMenu = false }) {
        DropdownMenuItem(
          text = { Text("Rename list") },
          onClick = {
            showOverflowMenu = false
            onIntent(TasksIntent.ShowRenameListDialog)
          },
        )
        DropdownMenuItem(
          text = { Text("Delete list") },
          onClick = {
            showOverflowMenu = false
            onIntent(TasksIntent.ShowDeleteListConfirmDialog)
          },
        )
      }
    }
  }
}
