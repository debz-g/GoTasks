package com.debzg.gotasks.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.debzg.gotasks.R
import com.debzg.gotasks.ui.theme.AccentCoral
import com.debzg.gotasks.ui.theme.SurfaceElevatedHigh
import com.debzg.gotasks.ui.theme.TextSecondary

val SheetShape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)

/**
 * Uniform start/end margin for every element in a sheet — text, checkbox, and icons alike.
 * `TextField` and `IconButton` both carry their own internal padding/touch-target inflation, so
 * [FlushTextField] and [SheetEdgeIconButton] exist to bypass that; otherwise their visible content
 * would sit deeper than this value.
 */
val SheetHorizontalPadding = 16.dp

/**
 * An icon button whose glyph sits flush against the [alignment] edge of its own box — plain
 * `IconButton` centers its icon inside a 48dp minimum touch target, which pushes the visible
 * glyph away from the true edge. This keeps a comfortable tap target while keeping the icon
 * itself flush at the row's start/end margin.
 */
@Composable
fun SheetEdgeIconButton(
  icon: Int,
  contentDescription: String?,
  tint: Color,
  onClick: () -> Unit,
  alignment: Alignment,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  boxSize: Dp = 36.dp,
  iconSize: Dp = 20.dp,
) {
  Box(modifier = modifier.size(boxSize).clickable(enabled = enabled, onClick = onClick), contentAlignment = alignment) {
    Icon(painter = painterResource(icon), contentDescription = contentDescription, tint = tint, modifier = Modifier.size(iconSize))
  }
}

/** Circular accent send/confirm button used at the trailing edge of a sheet's action row. */
@Composable
fun SheetSendButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Box(
    modifier =
      modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(if (enabled) AccentCoral else SurfaceElevatedHigh)
        .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      painter = painterResource(R.drawable.ic_send),
      contentDescription = "Save",
      tint = if (enabled) Color.White else TextSecondary,
      modifier = Modifier.size(18.dp),
    )
  }
}
