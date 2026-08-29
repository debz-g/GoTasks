package com.debzg.gotasks.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.debzg.gotasks.R
import com.debzg.gotasks.domain.model.Task
import com.debzg.gotasks.presentation.common.formatDueLabel
import com.debzg.gotasks.ui.theme.AccentCoral
import com.debzg.gotasks.ui.theme.SurfaceElevatedHigh
import com.debzg.gotasks.ui.theme.TextPrimary
import com.debzg.gotasks.ui.theme.TextSecondary

@Composable
fun TaskRow(task: Task, modifier: Modifier = Modifier, onToggleCompleted: () -> Unit = {}, onClick: () -> Unit = {}) {
  val startIndent = if (task.parentId != null) 40.dp else 16.dp

  Row(
    modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(start = startIndent, end = 16.dp, top = 12.dp, bottom = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    TaskCheckbox(isCompleted = task.isCompleted, onToggle = onToggleCompleted)

    Spacer(modifier = Modifier.width(16.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(text = task.title, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
      task.notes?.takeIf { it.isNotBlank() }?.let { notes ->
        Text(text = notes, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }
    }

    task.due?.let { due ->
      Spacer(modifier = Modifier.width(8.dp))
      Text(text = formatDueLabel(due, task.reminderTime), style = MaterialTheme.typography.labelSmall, color = AccentCoral)
    }
  }
}

@Composable
fun TaskCheckbox(isCompleted: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier, size: Dp = 22.dp) {
  Box(
    modifier =
      modifier
        .size(size)
        .clip(RoundedCornerShape(6.dp))
        .then(if (isCompleted) Modifier.background(SurfaceElevatedHigh) else Modifier.border(2.dp, AccentCoral, RoundedCornerShape(6.dp)))
        .clickable(onClick = onToggle),
    contentAlignment = Alignment.Center,
  ) {
    if (isCompleted) {
      Icon(painter = painterResource(R.drawable.ic_check), contentDescription = null, tint = AccentCoral, modifier = Modifier.size(size * 0.64f))
    }
  }
}
