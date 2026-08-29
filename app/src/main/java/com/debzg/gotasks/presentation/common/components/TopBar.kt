package com.debzg.gotasks.presentation.common.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.debzg.gotasks.R
import com.debzg.gotasks.ui.theme.TextPrimary

@Composable
fun TopBar(title: String, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = title, style = MaterialTheme.typography.titleLarge, color = TextPrimary, modifier = Modifier.weight(1f))
    IconButton(onClick = onRefresh) {
      Icon(painter = painterResource(R.drawable.ic_refresh), contentDescription = "Refresh", tint = TextPrimary, modifier = Modifier.size(22.dp))
    }
  }
}
