package com.debzg.gotasks.presentation.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatDueDate(instant: Instant): String {
  val zone = ZoneId.systemDefault()
  val date = instant.atZone(zone).toLocalDate()
  val today = LocalDate.now(zone)
  return when (date) {
    today -> "Today"
    today.plusDays(1) -> "Tomorrow"
    else -> date.format(DateTimeFormatter.ofPattern("MMM d"))
  }
}
