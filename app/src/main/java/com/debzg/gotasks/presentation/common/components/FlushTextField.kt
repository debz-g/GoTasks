package com.debzg.gotasks.presentation.common.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import com.debzg.gotasks.ui.theme.AccentCoral
import com.debzg.gotasks.ui.theme.TextPrimary
import com.debzg.gotasks.ui.theme.TextSecondary

/**
 * Borderless text field with zero built-in padding — unlike Material3's `TextField`, which
 * always reserves ~16dp of internal content padding with no way to override it on the
 * value/onValueChange overload. Used in the quick-add/edit sheets where every element (text,
 * checkbox, icons) needs to align flush to the same margin.
 */
@Composable
fun FlushTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, textStyle: TextStyle, modifier: Modifier = Modifier) {
  Box(modifier = Modifier.fillMaxWidth()) {
    BasicTextField(
      value = value,
      onValueChange = onValueChange,
      textStyle = textStyle.copy(color = TextPrimary),
      cursorBrush = SolidColor(AccentCoral),
      modifier = modifier.fillMaxWidth(),
    )
    if (value.isEmpty()) {
      Text(text = placeholder, style = textStyle, color = TextSecondary)
    }
  }
}
