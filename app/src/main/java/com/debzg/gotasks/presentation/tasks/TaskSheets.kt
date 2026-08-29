package com.debzg.gotasks.presentation.tasks

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.debzg.gotasks.R
import com.debzg.gotasks.domain.model.Task
import com.debzg.gotasks.presentation.common.components.FlushTextField
import com.debzg.gotasks.presentation.common.components.TaskCheckbox
import com.debzg.gotasks.presentation.common.formatDueDate
import com.debzg.gotasks.ui.theme.AccentCoral
import com.debzg.gotasks.ui.theme.SurfaceElevated
import com.debzg.gotasks.ui.theme.SurfaceElevatedHigh
import com.debzg.gotasks.ui.theme.TextPrimary
import com.debzg.gotasks.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private val SheetShape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
private val HeaderSlideSpec: FiniteAnimationSpec<Dp> = tween(durationMillis = 450)
private val HeaderFadeSpec: FiniteAnimationSpec<Float> = tween(durationMillis = 380)

// Back-arrow box (36dp) plus a small gap — how far the list name shifts right to make room for it.
private val HeaderSlideDistance = 44.dp

// Uniform 16dp start/end margin for every element in the sheet — text, checkbox, and icons alike.
// TextField and IconButton both carry their own internal padding/touch-target inflation, so both
// are overridden below (zero content padding on TextField, flush-aligned edge icons) rather than
// just relying on this column padding, or their visible content would sit deeper than 16dp.
private val SheetHorizontalPadding = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskSheet(
    listTitle: String,
    onConfirm: (title: String, notes: String?, isStarred: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showDetails by remember { mutableStateOf(false) }
    var isStarred by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceElevated,
        dragHandle = null,
        shape = SheetShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SheetHorizontalPadding, vertical = 4.dp)
        ) {

            Spacer(modifier = Modifier.height(16.dp))
            FlushTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = "What would you like to do?",
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.focusRequester(focusRequester),
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (showDetails) {
                FlushTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Add details",
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                EdgeIconButton(
                    icon = R.drawable.ic_details,
                    contentDescription = "Add details",
                    tint = if (showDetails) AccentCoral else TextSecondary,
                    alignment = Alignment.CenterStart,
                    onClick = { showDetails = !showDetails },
                )
                EdgeIconButton(
                    icon = if (isStarred) R.drawable.ic_star_filled else R.drawable.ic_star_outline,
                    contentDescription = "Star",
                    tint = if (isStarred) AccentCoral else TextSecondary,
                    alignment = Alignment.CenterStart,
                    onClick = { isStarred = !isStarred },
                    iconSize = 24.dp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = listTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.weight(1f))
                SendButton(
                    enabled = title.isNotBlank(),
                    onClick = {
                        if (title.isNotBlank()) onConfirm(
                            title,
                            notes.ifBlank { null },
                            isStarred
                        )
                    })
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskSheet(
    task: Task,
    listTitle: String,
    onSave: (title: String, notes: String?, isStarred: Boolean) -> Unit,
    onDelete: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDismiss: () -> Unit,
) {
    // Not skipping partial expansion: the sheet opens peeking (like TickTick/Google Tasks), and
    // dragging it up to full height slides a back arrow in ahead of the list name.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    val isFullyExpanded = sheetState.targetValue == SheetValue.Expanded
    var title by remember { mutableStateOf(task.title) }
    var notes by remember { mutableStateOf(task.notes.orEmpty()) }
    var isStarred by remember { mutableStateOf(task.isStarred) }
    var showOptions by remember { mutableStateOf(false) }

    fun saveAndDismiss() {
        if (title.isNotBlank()) onSave(title, notes.ifBlank { null }, isStarred)
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = { saveAndDismiss() },
        sheetState = sheetState,
        containerColor = SurfaceElevated,
        dragHandle = null,
        shape = SheetShape,
    ) {
        // fillMaxHeight (not just wrap-content) so the sheet actually has room to grow into when
        // dragged to Expanded — otherwise Material3 renders both states at the same short intrinsic height.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(start = SheetHorizontalPadding, end = SheetHorizontalPadding, top = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // The list name stays visible in both states — expanding just slides it right to make room
                // for the back arrow, which slides in from off-screen left. Nothing is replaced or hidden.
                val nameOffset by animateDpAsState(
                    if (isFullyExpanded) HeaderSlideDistance else 0.dp,
                    HeaderSlideSpec,
                    label = "listNameOffset"
                )
                val backOffset by animateDpAsState(
                    if (isFullyExpanded) 0.dp else -HeaderSlideDistance,
                    HeaderSlideSpec,
                    label = "backIconOffset"
                )
                val backAlpha by animateFloatAsState(
                    if (isFullyExpanded) 1f else 0f,
                    HeaderFadeSpec,
                    label = "backIconAlpha"
                )

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    EdgeIconButton(
                        icon = R.drawable.ic_arrow_back,
                        contentDescription = "Collapse",
                        tint = TextPrimary,
                        alignment = Alignment.CenterStart,
                        enabled = isFullyExpanded,
                        onClick = { scope.launch { sheetState.partialExpand() } },
                        modifier = Modifier
                            .offset(x = backOffset)
                            .alpha(backAlpha),
                    )
                    Text(
                        text = listTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        modifier = Modifier.offset(x = nameOffset),
                    )
                }
                EdgeIconButton(
                    icon = R.drawable.ic_more_vert,
                    contentDescription = "More",
                    tint = TextPrimary,
                    alignment = Alignment.CenterEnd,
                    onClick = { showOptions = true },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                TaskCheckbox(isCompleted = task.isCompleted, onToggle = onToggleCompleted)
                Spacer(modifier = Modifier.width(12.dp))
                task.due?.let { due ->
                    Text(
                        text = formatDueDate(due),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentCoral
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FlushTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = "Title",
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlushTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = "Add details",
                textStyle = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showOptions) {
        TaskOptionsSheet(
            isCompleted = task.isCompleted,
            isStarred = isStarred,
            onToggleStarred = {
                showOptions = false
                isStarred = !isStarred
            },
            onToggleCompleted = {
                showOptions = false
                onToggleCompleted()
            },
            onDelete = {
                showOptions = false
                onDelete()
            },
            onDismiss = { showOptions = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskOptionsSheet(
    isCompleted: Boolean,
    isStarred: Boolean,
    onToggleStarred: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceElevated,
        dragHandle = null,
        shape = SheetShape
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)) {
            OptionRow(
                icon = if (isStarred) R.drawable.ic_star_filled else R.drawable.ic_star_outline,
                label = if (isStarred) "Remove star" else "Star task",
                tint = if (isStarred) AccentCoral else TextPrimary,
                onClick = onToggleStarred,
            )
            OptionRow(
                icon = R.drawable.ic_check,
                label = if (isCompleted) "Mark as incomplete" else "Mark as complete",
                onClick = onToggleCompleted,
            )
            OptionRow(
                icon = R.drawable.ic_delete,
                label = "Delete",
                tint = MaterialTheme.colorScheme.error,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun OptionRow(icon: Int, label: String, onClick: () -> Unit, tint: Color = TextPrimary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SheetHorizontalPadding, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = tint)
    }
}

/**
 * An icon button whose glyph sits flush against the [alignment] edge of its own box — plain
 * `IconButton` centers its icon inside a 48dp minimum touch target, which pushes the visible
 * glyph away from the true edge of whatever it's aligned within. This keeps a comfortable tap
 * target (36dp) while keeping the icon itself flush at the row's start/end margin.
 */
@Composable
private fun EdgeIconButton(
    icon: Int,
    contentDescription: String?,
    tint: Color,
    onClick: () -> Unit,
    alignment: Alignment,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    boxSize: Dp = 36.dp,
    iconSize: Dp = 20.dp,
) {
    Box(
        modifier = modifier
            .size(boxSize)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = alignment
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun SendButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (enabled) AccentCoral else SurfaceElevatedHigh)
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_send),
            contentDescription = "Save",
            tint = if (enabled) Color.White else TextSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}
