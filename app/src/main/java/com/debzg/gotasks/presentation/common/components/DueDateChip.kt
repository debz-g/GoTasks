package com.debzg.gotasks.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.debzg.gotasks.R
import com.debzg.gotasks.presentation.common.formatDueLabel
import com.debzg.gotasks.ui.theme.AccentCoral
import java.time.LocalDateTime

/** Shows the date recognised from the task text, with an × to reject it. */
@Composable
fun DueDateChip(dateTime: LocalDateTime, hasTime: Boolean, onClear: () -> Unit, modifier: Modifier = Modifier) {
  Row(
    modifier =
      modifier
        .clip(RoundedCornerShape(8.dp))
        .background(AccentCoral.copy(alpha = 0.15f))
        .border(1.dp, AccentCoral.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
        .padding(horizontal = 10.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = formatDueLabel(dateTime, hasTime), style = MaterialTheme.typography.labelSmall, color = AccentCoral)
    Spacer(modifier = Modifier.width(6.dp))
    Icon(
      painter = painterResource(R.drawable.ic_close),
      contentDescription = "Clear due date",
      tint = AccentCoral,
      modifier = Modifier.size(14.dp).clickable(onClick = onClear),
    )
  }
}

