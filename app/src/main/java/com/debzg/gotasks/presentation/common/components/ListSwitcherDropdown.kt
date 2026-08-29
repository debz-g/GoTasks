package com.debzg.gotasks.presentation.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import com.debzg.gotasks.domain.model.TaskList
import com.debzg.gotasks.ui.theme.TextPrimary

@Composable
fun ListSwitcherDropdown(
  title: String,
  isExpanded: Boolean,
  taskLists: List<TaskList>,
  onToggle: () -> Unit,
  onSelect: (String) -> Unit,
  onNewList: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    Row(modifier = Modifier.clickable(onClick = onToggle), verticalAlignment = Alignment.CenterVertically) {
      Text(text = title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
      Icon(
        painter = painterResource(R.drawable.ic_chevron_down),
        contentDescription = null,
        tint = TextPrimary,
        modifier = Modifier.size(20.dp).rotate(if (isExpanded) 180f else 0f),
      )
    }
    DropdownMenu(expanded = isExpanded, onDismissRequest = onToggle) {
      taskLists.forEach { list -> DropdownMenuItem(text = { Text(list.title) }, onClick = { onSelect(list.id) }) }
      HorizontalDivider()
      DropdownMenuItem(text = { Text("New list") }, onClick = onNewList)
    }
  }
}
