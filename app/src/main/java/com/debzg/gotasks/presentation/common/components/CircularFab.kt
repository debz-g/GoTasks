package com.debzg.gotasks.presentation.common.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.debzg.gotasks.R
import com.debzg.gotasks.ui.theme.AccentCoral

@Composable
fun CircularFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
  FloatingActionButton(onClick = onClick, containerColor = AccentCoral, contentColor = Color.White, shape = CircleShape, modifier = modifier) {
    Icon(painter = painterResource(R.drawable.ic_add), contentDescription = "Add task")
  }
}
