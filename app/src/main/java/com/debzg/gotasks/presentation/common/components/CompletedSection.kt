package com.debzg.gotasks.presentation.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.debzg.gotasks.R
import com.debzg.gotasks.domain.model.Task
import com.debzg.gotasks.ui.theme.TextSecondary

@Composable
fun CompletedSection(
  completedTasks: List<Task>,
  isExpanded: Boolean,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier,
  onToggleTaskCompleted: (Task) -> Unit = {},
  onTaskClick: (Task) -> Unit = {},
) {
  if (completedTasks.isEmpty()) return

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "COMPLETED",
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary,
        modifier = Modifier.weight(1f),
      )
      Text(text = "${completedTasks.size}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
      Spacer(modifier = Modifier.width(4.dp))
      Icon(
        painter = painterResource(R.drawable.ic_chevron_down),
        contentDescription = null,
        tint = TextSecondary,
        modifier = Modifier.size(18.dp).rotate(if (isExpanded) 180f else 0f),
      )
    }

    if (isExpanded) {
      completedTasks.forEach { task ->
        TaskRow(task = task, onToggleCompleted = { onToggleTaskCompleted(task) }, onClick = { onTaskClick(task) })
      }
    }
  }
}
