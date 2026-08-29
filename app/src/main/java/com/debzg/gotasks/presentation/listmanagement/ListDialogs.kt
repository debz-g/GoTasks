package com.debzg.gotasks.presentation.listmanagement

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun NewListDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
  var title by remember { mutableStateOf("") }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("New list") },
    text = { OutlinedTextField(value = title, onValueChange = { title = it }, singleLine = true, placeholder = { Text("List name") }) },
    confirmButton = { TextButton(onClick = { onConfirm(title) }) { Text("Create") } },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

@Composable
fun RenameListDialog(currentTitle: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
  var title by remember { mutableStateOf(currentTitle) }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Rename list") },
    text = { OutlinedTextField(value = title, onValueChange = { title = it }, singleLine = true) },
    confirmButton = { TextButton(onClick = { onConfirm(title) }) { Text("Save") } },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

@Composable
fun DeleteListConfirmDialog(listTitle: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Delete \"$listTitle\"?") },
    text = { Text("All tasks in this list will be removed. This can't be undone.") },
    confirmButton = { TextButton(onClick = onConfirm) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}
