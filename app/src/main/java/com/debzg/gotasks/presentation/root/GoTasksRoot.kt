package com.debzg.gotasks.presentation.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.debzg.gotasks.data.auth.AuthState
import com.debzg.gotasks.data.auth.AuthStateRepository
import com.debzg.gotasks.data.auth.SessionRestorer
import com.debzg.gotasks.presentation.signin.SignInScreen
import com.debzg.gotasks.presentation.tasks.TasksScreen
import com.debzg.gotasks.ui.theme.AccentCoral
import org.koin.compose.koinInject

@Composable
fun GoTasksRoot(
  modifier: Modifier = Modifier,
  authStateRepository: AuthStateRepository = koinInject(),
  sessionRestorer: SessionRestorer = koinInject(),
) {
  val authState by authStateRepository.state.collectAsState()

  LaunchedEffect(Unit) {
    if (authStateRepository.state.value is AuthState.Restoring) sessionRestorer.restore()
  }

  when (authState) {
    is AuthState.Restoring -> {
      Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AccentCoral) }
    }
    is AuthState.Authorized -> TasksScreen(modifier = modifier.fillMaxSize())
    is AuthState.SignedOut,
    is AuthState.NeedsReauthorization -> SignInScreen(modifier = modifier.fillMaxSize())
  }
}
