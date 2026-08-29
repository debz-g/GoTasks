package com.debzg.gotasks.presentation.common.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Tints the recognised date phrase inside the text field as the user types.
 *
 * Styling only — the text itself is untouched, so [OffsetMapping.Identity] is correct and the
 * cursor and selection keep behaving normally. Applying this as a transformation (rather than
 * storing an AnnotatedString as the field's value) is what keeps editing unaffected.
 */
class DateHighlightVisualTransformation(private val ranges: List<IntRange>, private val color: Color) : VisualTransformation {

  override fun filter(text: AnnotatedString): TransformedText {
    if (ranges.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

    val styled = buildAnnotatedString {
      append(text)
      ranges.forEach { range ->
        // Ranges come from a parse of a possibly-older string, so clamp before styling.
        val start = range.first.coerceIn(0, text.length)
        val end = (range.last + 1).coerceIn(0, text.length)
        if (start < end) addStyle(SpanStyle(color = color, fontWeight = FontWeight.SemiBold), start, end)
      }
    }
    return TransformedText(styled, OffsetMapping.Identity)
  }
}
