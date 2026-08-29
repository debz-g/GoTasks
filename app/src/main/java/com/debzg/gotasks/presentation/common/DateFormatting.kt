package com.debzg.gotasks.presentation.common

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a")

/** "Today" / "Tomorrow" / "Fri, 5 Sep", with the year appended once it isn't the current one. */
fun formatDueDate(dateTime: LocalDateTime, today: LocalDate = LocalDate.now()): String {
  val date = dateTime.toLocalDate()
  return when (date) {
    today -> "Today"
    today.plusDays(1) -> "Tomorrow"
    else -> date.format(DateTimeFormatter.ofPattern(if (date.year == today.year) "EEE, d MMM" else "d MMM yyyy"))
  }
}

fun formatDueDate(instant: Instant, zone: ZoneId = ZoneId.systemDefault(), today: LocalDate = LocalDate.now(zone)): String =
  formatDueDate(instant.atZone(zone).toLocalDateTime(), today)

/** Adds the time when one was set — the API's `due` is date-only, so a time means a local reminder. */
fun formatDueLabel(dateTime: LocalDateTime, hasTime: Boolean, today: LocalDate = LocalDate.now()): String {
  val day = formatDueDate(dateTime, today)
  return if (hasTime) "$day · ${dateTime.toLocalTime().format(TIME_FORMAT)}" else day
}

/**
 * Label for a task row: the due date, plus the reminder time when the user set one.
 *
 * [reminderTime] is the authoritative source for the time of day. `due` is normalised to UTC
 * midnight for the API, so reading a time off it would show the wrong thing.
 */
fun formatDueLabel(
  due: Instant,
  reminderTime: Instant?,
  zone: ZoneId = ZoneId.systemDefault(),
  today: LocalDate = LocalDate.now(zone),
): String {
  val dayLabel = formatDueDate(due.atZone(zone).toLocalDateTime(), today)
  val time = reminderTime?.atZone(zone)?.toLocalTime() ?: return dayLabel
  return "$dayLabel · ${time.format(TIME_FORMAT)}"
}
