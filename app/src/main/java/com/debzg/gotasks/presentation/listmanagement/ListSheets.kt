package com.debzg.gotasks.presentation.listmanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.debzg.gotasks.R
import com.debzg.gotasks.domain.model.TaskList
import com.debzg.gotasks.presentation.common.components.FlushTextField
import com.debzg.gotasks.presentation.common.components.SheetHorizontalPadding
import com.debzg.gotasks.presentation.common.components.SheetSendButton
import com.debzg.gotasks.presentation.common.components.SheetShape
import com.debzg.gotasks.ui.theme.AccentCoral
import com.debzg.gotasks.ui.theme.SurfaceElevated
import com.debzg.gotasks.ui.theme.SurfaceElevatedHigh
import com.debzg.gotasks.ui.theme.TextPrimary
import com.debzg.gotasks.ui.theme.TextSecondary

/**
 * List switcher. The rows scroll while the "Create new list" card stays pinned to the bottom, so
 * it's reachable no matter how many lists there are.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListPickerSheet(
  taskLists: List<TaskList>,
  activeTaskListId: String?,
  onSelect: (String) -> Unit,
  onCreateNewList: () -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = SurfaceElevated, dragHandle = null, shape = SheetShape) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SheetHorizontalPadding, vertical = 20.dp)) {
      Text(text = "Lists", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

      Spacer(modifier = Modifier.height(12.dp))

      // Capped so a long list still leaves the create card visible without swallowing the screen.
      LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
        items(taskLists, key = { it.id }) { list ->
          ListRow(title = list.title, isActive = list.id == activeTaskListId, onClick = { onSelect(list.id) })
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      CreateListCard(onClick = onCreateNewList)
    }
  }
}

@Composable
private fun ListRow(title: String, isActive: Boolean, onClick: () -> Unit) {
  Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
    Text(
      text = title,
      style = MaterialTheme.typography.bodyLarge,
      color = if (isActive) AccentCoral else TextPrimary,
      modifier = Modifier.weight(1f),
    )
    if (isActive) {
      Icon(painter = painterResource(R.drawable.ic_check), contentDescription = null, tint = AccentCoral, modifier = Modifier.size(18.dp))
    }
  }
}

@Composable
private fun CreateListCard(onClick: () -> Unit) {
  Row(
    modifier =
      Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceElevatedHigh).clickable(onClick = onClick).padding(16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(painter = painterResource(R.drawable.ic_add), contentDescription = null, tint = AccentCoral, modifier = Modifier.size(20.dp))
    Spacer(modifier = Modifier.width(12.dp))
    Text(text = "Create new list", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
  }
}

/** Compact sheet for naming a new list. The keyboard's Done key saves. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewListSheet(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var title by remember { mutableStateOf("") }
  val focusRequester = remember { FocusRequester() }

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = SurfaceElevated, dragHandle = null, shape = SheetShape) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SheetHorizontalPadding, vertical = 20.dp)) {
      Text(text = "New list", style = MaterialTheme.typography.titleMedium, color = TextSecondary)

      Spacer(modifier = Modifier.height(16.dp))

      FlushTextField(
        value = title,
        onValueChange = { title = it },
        placeholder = "List name",
        textStyle = MaterialTheme.typography.bodyLarge,
        singleLine = true,
        imeAction = ImeAction.Done,
        onImeAction = { if (title.isNotBlank()) onConfirm(title) },
        modifier = Modifier.focusRequester(focusRequester),
      )

      Spacer(modifier = Modifier.height(24.dp))

      Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.weight(1f))
        SheetSendButton(enabled = title.isNotBlank(), onClick = { if (title.isNotBlank()) onConfirm(title) })
      }
    }
  }

  LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

/** Compact sheet for renaming the current list. The keyboard's Done key saves. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameListSheet(currentTitle: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var title by remember { mutableStateOf(currentTitle) }
  val focusRequester = remember { FocusRequester() }

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = SurfaceElevated, dragHandle = null, shape = SheetShape) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SheetHorizontalPadding, vertical = 20.dp)) {
      Text(text = "Rename list", style = MaterialTheme.typography.titleMedium, color = TextSecondary)

      Spacer(modifier = Modifier.height(16.dp))

      FlushTextField(
        value = title,
        onValueChange = { title = it },
        placeholder = "List name",
        textStyle = MaterialTheme.typography.bodyLarge,
        singleLine = true,
        imeAction = ImeAction.Done,
        onImeAction = { if (title.isNotBlank()) onConfirm(title) },
        modifier = Modifier.focusRequester(focusRequester),
      )

      Spacer(modifier = Modifier.height(24.dp))

      Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.weight(1f))
        SheetSendButton(enabled = title.isNotBlank(), onClick = { if (title.isNotBlank()) onConfirm(title) })
      }
    }
  }

  LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

@Composable
fun DeleteListConfirmDialog(listTitle: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = SurfaceElevated,
    title = { Text("Delete \"$listTitle\"?", color = TextPrimary) },
    text = { Text("All tasks in this list will be removed. This can't be undone.", color = TextSecondary) },
    confirmButton = { TextButton(onClick = onConfirm) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } },
  )
}
