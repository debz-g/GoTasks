package com.debzg.gotasks.presentation.common

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class DateFormattingTest {

  private val today = LocalDate.of(2026, 8, 30)
  private val zone: ZoneId = ZoneId.of("Asia/Kolkata")

  private fun instantAt(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0) =
    LocalDateTime.of(year, month, day, hour, minute).atZone(zone).toInstant()

  @Test
  fun `names today and tomorrow`() {
    assertEquals("Today", formatDueDate(LocalDateTime.of(2026, 8, 30, 0, 0), today))
    assertEquals("Tomorrow", formatDueDate(LocalDateTime.of(2026, 8, 31, 0, 0), today))
  }

  @Test
  fun `other dates in this year omit the year`() {
    assertEquals("Sat, 5 Sep", formatDueDate(LocalDateTime.of(2026, 9, 5, 0, 0), today))
  }

  @Test
  fun `dates in another year include it`() {
    assertEquals("5 Sep 2027", formatDueDate(LocalDateTime.of(2027, 9, 5, 0, 0), today))
  }

  @Test
  fun `a reminder time is appended to the day label`() {
    val due = instantAt(2026, 8, 31)
    val reminder = instantAt(2026, 8, 31, 17, 30)
    // Meridiem casing differs between the JVM's CLDR data and Android's ICU, so ignore case.
    assertEquals("TOMORROW · 5:30 PM", formatDueLabel(due, reminder, zone, today).uppercase())
  }

  @Test
  fun `no reminder time leaves the label as the day alone`() {
    // Regression: a due date is stored at UTC midnight for the API, so reading a time off it would
    // print a bogus one. Only an explicit reminder time may contribute the time portion.
    val due = instantAt(2026, 8, 31)
    assertEquals("Tomorrow", formatDueLabel(due, reminderTime = null, zone = zone, today = today))
  }

  @Test
  fun `hasTime flag drives the LocalDateTime overload`() {
    val dateTime = LocalDateTime.of(2026, 8, 31, 9, 5)
    assertEquals("TOMORROW · 9:05 AM", formatDueLabel(dateTime, hasTime = true, today = today).uppercase())
    assertEquals("Tomorrow", formatDueLabel(dateTime, hasTime = false, today = today))
  }
}
