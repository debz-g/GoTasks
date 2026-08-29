package com.debzg.gotasks.datetime

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

/**
 * Recognises date and time phrases in free text, so typing "get groceries tomorrow at 5pm" sets a
 * due date without touching a picker.
 *
 * Deliberately rule-based rather than ML-backed: the phrase set people actually type is small and
 * predictable, and a table of regexes is inspectable, instant, and needs no model download.
 *
 * Pure Kotlin with no Android dependencies, so it's directly unit-testable.
 */
class DateTimeParser {

  fun parse(input: String, now: LocalDateTime = LocalDateTime.now()): ParsedDate? {
    val today = now.toLocalDate()

    // Rules are tried in order, so more specific phrases win over substrings of themselves
    // ("day after tomorrow" before "tomorrow", "next friday" before "friday").
    val dateMatch = dateRules.firstNotNullOfOrNull { rule -> rule.firstMatch(input, today) } ?: return null

    // Time is matched independently, skipping whatever the date rule already claimed.
    val timeMatch = findTime(input, exclude = dateMatch.range)

    val time = timeMatch?.time ?: LocalTime.MIDNIGHT
    return ParsedDate(
      dateRange = dateMatch.range,
      timeRange = timeMatch?.range,
      resolved = LocalDateTime.of(dateMatch.date, time),
      hasTime = timeMatch != null,
    )
  }

  private fun findTime(input: String, exclude: IntRange): TimeMatch? =
    timeRules.firstNotNullOfOrNull { rule ->
      rule.regex.findAll(input).firstNotNullOfOrNull { match ->
        // Ignore anything sitting inside the date phrase (e.g. the "12" of "5/12").
        if (match.range.first in exclude || match.range.last in exclude) return@firstNotNullOfOrNull null
        rule.resolve(match)?.let { TimeMatch(match.range, it) }
      }
    }

  private data class DateMatch(val range: IntRange, val date: LocalDate)

  private data class TimeMatch(val range: IntRange, val time: LocalTime)

  private class DateRule(pattern: String, val resolve: (MatchResult, LocalDate) -> LocalDate?) {
    val regex = Regex(pattern, RegexOption.IGNORE_CASE)

    fun firstMatch(input: String, today: LocalDate): DateMatch? =
      regex.findAll(input).firstNotNullOfOrNull { match -> resolve(match, today)?.let { DateMatch(match.range, it) } }
  }

  private class TimeRule(pattern: String, val resolve: (MatchResult) -> LocalTime?) {
    val regex = Regex(pattern, RegexOption.IGNORE_CASE)
  }

  private companion object {
    val WEEKDAYS =
      mapOf(
        "monday" to DayOfWeek.MONDAY,
        "mon" to DayOfWeek.MONDAY,
        "tuesday" to DayOfWeek.TUESDAY,
        "tue" to DayOfWeek.TUESDAY,
        "tues" to DayOfWeek.TUESDAY,
        "wednesday" to DayOfWeek.WEDNESDAY,
        "wed" to DayOfWeek.WEDNESDAY,
        "thursday" to DayOfWeek.THURSDAY,
        "thu" to DayOfWeek.THURSDAY,
        "thur" to DayOfWeek.THURSDAY,
        "thurs" to DayOfWeek.THURSDAY,
        "friday" to DayOfWeek.FRIDAY,
        "fri" to DayOfWeek.FRIDAY,
        "saturday" to DayOfWeek.SATURDAY,
        "sat" to DayOfWeek.SATURDAY,
        "sunday" to DayOfWeek.SUNDAY,
        "sun" to DayOfWeek.SUNDAY,
      )

    val MONTHS =
      mapOf(
        "january" to 1, "jan" to 1,
        "february" to 2, "feb" to 2,
        "march" to 3, "mar" to 3,
        "april" to 4, "apr" to 4,
        "may" to 5,
        "june" to 6, "jun" to 6,
        "july" to 7, "jul" to 7,
        "august" to 8, "aug" to 8,
        "september" to 9, "sep" to 9, "sept" to 9,
        "october" to 10, "oct" to 10,
        "november" to 11, "nov" to 11,
        "december" to 12, "dec" to 12,
      )

    private val WEEKDAY_ALT = WEEKDAYS.keys.sortedByDescending { it.length }.joinToString("|")
    private val MONTH_ALT = MONTHS.keys.sortedByDescending { it.length }.joinToString("|")

    /** Interprets a bare year: "26" → 2026. */
    private fun normaliseYear(raw: String): Int = raw.toInt().let { if (raw.length <= 2) 2000 + it else it }

    /** Builds a date, rolling to next year when the day/month has already passed this year. */
    private fun dateInSensibleYear(today: LocalDate, month: Int, day: Int): LocalDate? {
      val thisYear = runCatching { LocalDate.of(today.year, month, day) }.getOrNull() ?: return null
      return if (thisYear.isBefore(today)) thisYear.plusYears(1) else thisYear
    }

    val dateRules =
      listOf(
        // "day after tomorrow" must precede the plain "tomorrow" rule.
        DateRule("""\bday\s+after\s+tomorrow\b""") { _, today -> today.plusDays(2) },
        DateRule("""\bnext\s+week\b""") { _, today -> today.plusWeeks(1) },
        // "next friday" — strictly the following one, so it never resolves to today.
        DateRule("""\bnext\s+($WEEKDAY_ALT)\b""") { m, today ->
          WEEKDAYS[m.groupValues[1].lowercase()]?.let { today.with(TemporalAdjusters.next(it)) }
        },
        DateRule("""\bthis\s+($WEEKDAY_ALT)\b""") { m, today ->
          WEEKDAYS[m.groupValues[1].lowercase()]?.let { today.with(TemporalAdjusters.nextOrSame(it)) }
        },
        DateRule("""\bin\s+(\d{1,3})\s+day(?:s)?\b""") { m, today -> today.plusDays(m.groupValues[1].toLong()) },
        DateRule("""\bin\s+(\d{1,3})\s+week(?:s)?\b""") { m, today -> today.plusWeeks(m.groupValues[1].toLong()) },
        DateRule("""\bin\s+(\d{1,3})\s+month(?:s)?\b""") { m, today -> today.plusMonths(m.groupValues[1].toLong()) },
        // Day-first numeric dates (25/12, 25-12-2026) — the convention where this app is used.
        DateRule("""\b(\d{1,2})[/-](\d{1,2})(?:[/-](\d{2,4}))?\b""") { m, today ->
          val day = m.groupValues[1].toInt()
          val month = m.groupValues[2].toInt()
          val yearText = m.groupValues[3]
          if (month !in 1..12 || day !in 1..31) return@DateRule null
          if (yearText.isEmpty()) dateInSensibleYear(today, month, day)
          else runCatching { LocalDate.of(normaliseYear(yearText), month, day) }.getOrNull()
        },
        // "dec 25" / "december 25th"
        DateRule("""\b($MONTH_ALT)\s+(\d{1,2})(?:st|nd|rd|th)?\b""") { m, today ->
          val month = MONTHS[m.groupValues[1].lowercase()] ?: return@DateRule null
          dateInSensibleYear(today, month, m.groupValues[2].toInt())
        },
        // "25 dec" / "25th december"
        DateRule("""\b(\d{1,2})(?:st|nd|rd|th)?\s+($MONTH_ALT)\b""") { m, today ->
          val month = MONTHS[m.groupValues[2].lowercase()] ?: return@DateRule null
          dateInSensibleYear(today, month, m.groupValues[1].toInt())
        },
        DateRule("""\btom(?:orrow|morow|oro)?\b|\btmrw?\b""") { _, today -> today.plusDays(1) },
        DateRule("""\btoday\b|\btonight\b""") { _, today -> today },
        // Bare weekday resolves to the nearest upcoming one, today included.
        DateRule("""\b($WEEKDAY_ALT)\b""") { m, today ->
          WEEKDAYS[m.groupValues[1].lowercase()]?.let { today.with(TemporalAdjusters.nextOrSame(it)) }
        },
      )

    val timeRules =
      listOf(
        // 12-hour with meridiem: "at 5pm", "5:30 pm"
        TimeRule("""\b(?:at\s+)?(\d{1,2})(?::(\d{2}))?\s*(am|pm)\b""") { m ->
          val hour12 = m.groupValues[1].toInt()
          val minute = m.groupValues[2].ifEmpty { "0" }.toInt()
          if (hour12 !in 1..12 || minute !in 0..59) return@TimeRule null
          val isPm = m.groupValues[3].equals("pm", ignoreCase = true)
          val hour = if (isPm) (hour12 % 12) + 12 else hour12 % 12
          LocalTime.of(hour, minute)
        },
        // 24-hour, colon required so bare numbers ("buy 5 apples") never match.
        TimeRule("""\b(?:at\s+)?(\d{1,2}):(\d{2})\b""") { m ->
          val hour = m.groupValues[1].toInt()
          val minute = m.groupValues[2].toInt()
          if (hour !in 0..23 || minute !in 0..59) return@TimeRule null
          LocalTime.of(hour, minute)
        },
      )
  }
}
