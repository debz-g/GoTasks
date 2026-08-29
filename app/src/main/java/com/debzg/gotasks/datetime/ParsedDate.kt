package com.debzg.gotasks.datetime

import java.time.LocalDateTime

/**
 * A date (and optionally a time) recognised inside free text.
 *
 * The date and time spans are kept separate rather than merged into one range: in
 * "tomorrow buy milk at 5pm" the two phrases sit at opposite ends, so a single covering range
 * would swallow "buy milk" when the phrase is stripped from the title.
 */
data class ParsedDate(
  val dateRange: IntRange,
  val timeRange: IntRange?,
  val resolved: LocalDateTime,
  val hasTime: Boolean,
) {
  /** Every span that belongs to the date phrase, for highlighting and stripping. */
  val ranges: List<IntRange>
    get() = listOfNotNull(dateRange, timeRange)
}

/** Removes the recognised phrase(s), leaving just the task title. */
fun String.withoutParsedDate(parsed: ParsedDate): String {
  // Strip back-to-front so earlier indices stay valid.
  var result = this
  parsed.ranges.sortedByDescending { it.first }.forEach { range ->
    val start = range.first.coerceIn(0, result.length)
    val end = (range.last + 1).coerceIn(0, result.length)
    if (start < end) result = result.removeRange(start, end)
  }
  return result.replace(Regex("\\s{2,}"), " ").trim()
}
