package com.debzg.gotasks.datetime

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DateTimeParserTest {

  private val parser = DateTimeParser()

  // A Wednesday, so weekday tests have unambiguous expectations either side of "today".
  private val now = LocalDateTime.of(2026, 8, 26, 10, 0)

  private fun parse(input: String) = parser.parse(input, now)

  private fun assertResolves(input: String, expected: LocalDateTime) {
    val result = requireNotNull(parse(input)) { "expected \"$input\" to parse" }
    assertEquals(input, expected, result.resolved)
  }

  @Test
  fun `resolves today and tonight`() {
    assertResolves("submit report today", LocalDateTime.of(2026, 8, 26, 0, 0))
    assertResolves("call mom tonight", LocalDateTime.of(2026, 8, 26, 0, 0))
  }

  @Test
  fun `resolves tomorrow and its shorthands`() {
    val tomorrow = LocalDateTime.of(2026, 8, 27, 0, 0)
    assertResolves("get groceries tomorrow", tomorrow)
    assertResolves("get groceries tmrw", tomorrow)
    assertResolves("get groceries tmr", tomorrow)
  }

  @Test
  fun `day after tomorrow wins over the tomorrow rule`() {
    assertResolves("dentist day after tomorrow", LocalDateTime.of(2026, 8, 28, 0, 0))
  }

  @Test
  fun `bare weekday resolves to the nearest upcoming one`() {
    // Wednesday -> Friday is 2 days out.
    assertResolves("standup friday", LocalDateTime.of(2026, 8, 28, 0, 0))
  }

  @Test
  fun `bare weekday matching today resolves to today`() {
    assertResolves("gym wednesday", LocalDateTime.of(2026, 8, 26, 0, 0))
  }

  @Test
  fun `next weekday always skips past today`() {
    assertResolves("review next wednesday", LocalDateTime.of(2026, 9, 2, 0, 0))
    assertResolves("review next friday", LocalDateTime.of(2026, 8, 28, 0, 0))
  }

  @Test
  fun `resolves relative offsets`() {
    assertResolves("renew passport in 3 days", LocalDateTime.of(2026, 8, 29, 0, 0))
    assertResolves("check in 2 weeks", LocalDateTime.of(2026, 9, 9, 0, 0))
    assertResolves("review in 1 month", LocalDateTime.of(2026, 9, 26, 0, 0))
  }

  @Test
  fun `numeric dates are day first`() {
    // 12/9 is 12 September, not 9 December.
    assertResolves("pay rent 12/9", LocalDateTime.of(2026, 9, 12, 0, 0))
    assertResolves("pay rent 25-12-2026", LocalDateTime.of(2026, 12, 25, 0, 0))
  }

  @Test
  fun `numeric date already past this year rolls to next year`() {
    // 1 January has passed by late August, so it means next January.
    assertResolves("plan trip 1/1", LocalDateTime.of(2027, 1, 1, 0, 0))
  }

  @Test
  fun `resolves month name dates in both orders`() {
    assertResolves("buy gifts dec 25", LocalDateTime.of(2026, 12, 25, 0, 0))
    assertResolves("buy gifts 25th december", LocalDateTime.of(2026, 12, 25, 0, 0))
  }

  @Test
  fun `combines a date with a 12 hour time`() {
    val result = requireNotNull(parse("get groceries tomorrow at 5pm"))
    assertEquals(LocalDateTime.of(2026, 8, 27, 17, 0), result.resolved)
    assertTrue(result.hasTime)
  }

  @Test
  fun `parses minutes and the am meridiem`() {
    assertResolves("flight tomorrow 9:30 am", LocalDateTime.of(2026, 8, 27, 9, 30))
    assertResolves("call today at 12am", LocalDateTime.of(2026, 8, 26, 0, 0))
    assertResolves("call today at 12pm", LocalDateTime.of(2026, 8, 26, 12, 0))
  }

  @Test
  fun `parses 24 hour times`() {
    assertResolves("standup tomorrow at 17:00", LocalDateTime.of(2026, 8, 27, 17, 0))
  }

  @Test
  fun `a date without a time is midnight and flagged as timeless`() {
    val result = requireNotNull(parse("get groceries tomorrow"))
    assertFalse(result.hasTime)
    assertNull(result.timeRange)
  }

  @Test
  fun `bare numbers are not mistaken for times`() {
    // "5" here is a quantity; without am/pm or a colon it must not become a time.
    val result = requireNotNull(parse("buy 5 apples tomorrow"))
    assertFalse(result.hasTime)
    assertEquals(LocalDateTime.of(2026, 8, 27, 0, 0), result.resolved)
  }

  @Test
  fun `digits inside a numeric date are not re-read as a time`() {
    val result = requireNotNull(parse("pay rent 12/9"))
    assertFalse(result.hasTime)
  }

  @Test
  fun `text with no date phrase does not parse`() {
    assertNull(parse("buy milk"))
    assertNull(parse(""))
    assertNull(parse("email the 5 reports"))
  }

  @Test
  fun `stripping removes the phrase and leaves a clean title`() {
    val input = "get groceries tomorrow at 5pm"
    val result = requireNotNull(parse(input))
    assertEquals("get groceries", input.withoutParsedDate(result))
  }

  @Test
  fun `stripping handles date and time separated by other words`() {
    // The spans sit apart, so a single covering range would wrongly eat "buy milk".
    val input = "tomorrow buy milk at 5pm"
    val result = requireNotNull(parse(input))
    assertEquals("buy milk", input.withoutParsedDate(result))
  }

  @Test
  fun `matching is case insensitive`() {
    assertResolves("Get Groceries TOMORROW", LocalDateTime.of(2026, 8, 27, 0, 0))
  }

  @Test
  fun `date range covers exactly the matched phrase`() {
    val input = "get groceries tomorrow"
    val result = requireNotNull(parse(input))
    assertEquals("tomorrow", input.substring(result.dateRange.first, result.dateRange.last + 1))
  }
}
