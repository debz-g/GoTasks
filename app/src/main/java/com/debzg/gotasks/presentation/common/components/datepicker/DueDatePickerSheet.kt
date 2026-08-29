package com.debzg.gotasks.presentation.common.components.datepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.debzg.gotasks.R
import com.debzg.gotasks.presentation.common.components.SheetHorizontalPadding
import com.debzg.gotasks.presentation.common.components.SheetShape
import com.debzg.gotasks.ui.theme.AccentCoral
import com.debzg.gotasks.ui.theme.SurfaceElevated
import com.debzg.gotasks.ui.theme.SurfaceElevatedHigh
import com.debzg.gotasks.ui.theme.TextPrimary
import com.debzg.gotasks.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/** Everything the sheet returns when confirmed. */
data class DueDateSelection(val dateTime: LocalDateTime, val hasTime: Boolean)

/**
 * Due-date picker, modelled on TickTick's: a bottom sheet with a custom month calendar and rows
 * for time and repeat, rather than a stock Material date dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueDatePickerSheet(
  initial: LocalDateTime?,
  initialHasTime: Boolean,
  onConfirm: (DueDateSelection) -> Unit,
  onClear: () -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val today = remember { LocalDate.now() }

  var selectedDate by remember { mutableStateOf(initial?.toLocalDate() ?: today) }
  var displayedMonth by remember { mutableStateOf(YearMonth.from(initial?.toLocalDate() ?: today)) }
  var time by remember { mutableStateOf(initial?.toLocalTime()?.takeIf { initialHasTime }) }

  var showTimePicker by remember { mutableStateOf(false) }

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = SurfaceElevated, dragHandle = null, shape = SheetShape) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SheetHorizontalPadding).padding(top = 16.dp, bottom = 8.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
          painter = painterResource(R.drawable.ic_close),
          contentDescription = "Cancel",
          tint = TextPrimary,
          modifier = Modifier.size(24.dp).clickable(onClick = onDismiss),
        )
        Spacer(modifier = Modifier.width(24.dp))
        // Styled as the active tab from the reference, minus the Duration tab we don't support.
        Column {
          Text(text = "Date", style = MaterialTheme.typography.titleMedium, color = AccentCoral)
          Spacer(modifier = Modifier.height(4.dp))
          Box(modifier = Modifier.width(32.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(AccentCoral))
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(
          painter = painterResource(R.drawable.ic_check),
          contentDescription = "Confirm",
          tint = TextPrimary,
          modifier =
            Modifier.size(26.dp).clickable {
              onConfirm(
                DueDateSelection(dateTime = LocalDateTime.of(selectedDate, time ?: LocalTime.MIDNIGHT), hasTime = time != null)
              )
            },
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      CalendarGrid(
        displayedMonth = displayedMonth,
        selectedDate = selectedDate,
        onSelectDate = { selectedDate = it },
        onPreviousMonth = { displayedMonth = displayedMonth.minusMonths(1) },
        onNextMonth = { displayedMonth = displayedMonth.plusMonths(1) },
        today = today,
      )

      Spacer(modifier = Modifier.height(20.dp))

      Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceElevatedHigh)) {
        OptionRow(
          icon = R.drawable.ic_clock,
          label = "Time",
          value = time?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "None",
          isSet = time != null,
          onClick = { showTimePicker = true },
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Clear",
        style = MaterialTheme.typography.bodyLarge,
        color = AccentCoral,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClear).padding(vertical = 12.dp),
      )
    }
  }

  if (showTimePicker) {
    TimePickerDialog(
      initialTime = time ?: LocalTime.of(9, 0),
      onConfirm = {
        time = it
        showTimePicker = false
      },
      onClear = {
        time = null
        showTimePicker = false
      },
      onDismiss = { showTimePicker = false },
    )
  }

}

@Composable
private fun OptionRow(icon: Int, label: String, value: String, isSet: Boolean, onClick: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(painter = painterResource(icon), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
    Spacer(modifier = Modifier.width(16.dp))
    Text(text = label, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, modifier = Modifier.weight(1f))
    Text(text = value, style = MaterialTheme.typography.bodyMedium, color = if (isSet) AccentCoral else TextSecondary)
    Spacer(modifier = Modifier.width(6.dp))
    Icon(
      painter = painterResource(R.drawable.ic_chevron_right),
      contentDescription = null,
      tint = TextSecondary,
      modifier = Modifier.size(18.dp),
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(initialTime: LocalTime, onConfirm: (LocalTime) -> Unit, onClear: () -> Unit, onDismiss: () -> Unit) {
  val state = rememberTimePickerState(initialHour = initialTime.hour, initialMinute = initialTime.minute, is24Hour = false)

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = SurfaceElevatedHigh,
    title = { Text("Time", style = MaterialTheme.typography.titleMedium, color = TextPrimary) },
    text = {
      TimePicker(
        state = state,
        colors =
          TimePickerDefaults.colors(
            clockDialColor = SurfaceElevated,
            selectorColor = AccentCoral,
            containerColor = SurfaceElevatedHigh,
            periodSelectorSelectedContainerColor = AccentCoral.copy(alpha = 0.2f),
            periodSelectorSelectedContentColor = AccentCoral,
            timeSelectorSelectedContainerColor = AccentCoral.copy(alpha = 0.2f),
            timeSelectorSelectedContentColor = AccentCoral,
          ),
      )
    },
    confirmButton = { TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) { Text("OK", color = AccentCoral) } },
    dismissButton = {
      Row {
        TextButton(onClick = onClear) { Text("Clear", color = TextSecondary) }
        TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
      }
    },
  )
}

