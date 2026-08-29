package com.debzg.gotasks.presentation.common.components.datepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.debzg.gotasks.R
import com.debzg.gotasks.ui.theme.AccentCoral
import com.debzg.gotasks.ui.theme.TextPrimary
import com.debzg.gotasks.ui.theme.TextSecondary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val WEEK_START = DayOfWeek.MONDAY

/**
 * Month calendar with a Monday-first grid.
 *
 * Hand-rolled rather than using Material3's `DatePicker` because that component brings its own
 * header, mode toggles and container styling that can't be reshaped into this layout.
 */
@Composable
fun CalendarGrid(
  displayedMonth: YearMonth,
  selectedDate: LocalDate?,
  onSelectDate: (LocalDate) -> Unit,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  modifier: Modifier = Modifier,
  today: LocalDate = LocalDate.now(),
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = displayedMonth.format(DateTimeFormatter.ofPattern(if (displayedMonth.year == today.year) "MMMM" else "MMMM yyyy")),
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        modifier = Modifier.weight(1f),
      )
      Icon(
        painter = painterResource(R.drawable.ic_chevron_left),
        contentDescription = "Previous month",
        tint = TextSecondary,
        modifier = Modifier.size(28.dp).clip(CircleShape).clickable(onClick = onPreviousMonth).padding(4.dp),
      )
      Spacer(modifier = Modifier.size(16.dp))
      Icon(
        painter = painterResource(R.drawable.ic_chevron_right),
        contentDescription = "Next month",
        tint = TextSecondary,
        modifier = Modifier.size(28.dp).clip(CircleShape).clickable(onClick = onNextMonth).padding(4.dp),
      )
    }

    Row(modifier = Modifier.fillMaxWidth()) {
      weekdayOrder().forEach { day ->
        Text(
          text = day.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()),
          style = MaterialTheme.typography.labelSmall,
          color = TextSecondary,
          textAlign = TextAlign.Center,
          modifier = Modifier.weight(1f),
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Leading blanks so the 1st lands under the right weekday.
    val firstOfMonth = displayedMonth.atDay(1)
    val leadingBlanks = ((firstOfMonth.dayOfWeek.value - WEEK_START.value) + 7) % 7
    val cells = leadingBlanks + displayedMonth.lengthOfMonth()
    val rows = (cells + 6) / 7

    for (row in 0 until rows) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        for (column in 0 until 7) {
          val cellIndex = row * 7 + column
          val dayOfMonth = cellIndex - leadingBlanks + 1
          Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
            if (dayOfMonth in 1..displayedMonth.lengthOfMonth()) {
              val date = displayedMonth.atDay(dayOfMonth)
              DayCell(
                dayOfMonth = dayOfMonth,
                isSelected = date == selectedDate,
                isToday = date == today,
                onClick = { onSelectDate(date) },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DayCell(dayOfMonth: Int, isSelected: Boolean, isToday: Boolean, onClick: () -> Unit) {
  // Selection wins over the today marker when they land on the same day.
  val background =
    when {
      isSelected -> AccentCoral
      isToday -> Color.White
      else -> Color.Transparent
    }
  val contentColor =
    when {
      isSelected -> Color.White
      isToday -> Color.Black
      else -> TextPrimary
    }

  Box(
    modifier = Modifier.size(38.dp).clip(CircleShape).background(background).clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = dayOfMonth.toString(),
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal,
      color = contentColor,
    )
  }
}

private fun weekdayOrder(): List<DayOfWeek> = (0 until 7).map { WEEK_START.plus(it.toLong()) }
