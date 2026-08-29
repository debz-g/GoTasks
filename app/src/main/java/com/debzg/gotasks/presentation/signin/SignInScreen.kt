package com.debzg.gotasks.presentation.signin

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.debzg.gotasks.R
import com.debzg.gotasks.ui.theme.AccentCoral
import com.debzg.gotasks.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignInScreen(modifier: Modifier = Modifier, viewModel: SignInViewModel = koinViewModel()) {
  val state by viewModel.state.collectAsState()
  val activity = LocalContext.current as Activity

  val authorizationLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
      viewModel.onIntent(SignInIntent.AuthorizationResolved(result.data), activity)
    }

  LaunchedEffect(state.pendingAuthorizationIntentSender) {
    state.pendingAuthorizationIntentSender?.let { intentSender ->
      authorizationLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
      viewModel.onIntent(SignInIntent.AuthorizationIntentSenderConsumed, activity)
    }
  }

  Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
    Column(
      modifier = Modifier.widthIn(max = 360.dp).verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(modifier = Modifier.size(88.dp).clip(RoundedCornerShape(24.dp)).background(AccentCoral), contentAlignment = Alignment.Center) {
        Icon(painter = painterResource(R.drawable.ic_check), contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
      }

      Spacer(modifier = Modifier.height(32.dp))
      Text(text = "GoTasks", style = MaterialTheme.typography.headlineMedium)
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Your Google Tasks, reimagined.",
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondary,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(48.dp))

      if (state.signedInEmail == null) {
        GoogleSignInButton(onClick = { viewModel.onIntent(SignInIntent.SignIn, activity) }, enabled = !state.isLoading)
      } else {
        Text(text = "Signed in as ${state.signedInEmail}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = { viewModel.onIntent(SignInIntent.FetchTaskLists, activity) }, enabled = !state.isLoading) {
          Text(text = "Fetch task lists (debug)")
        }
      }

      if (state.isLoading) {
        Spacer(modifier = Modifier.height(24.dp))
        CircularProgressIndicator(color = AccentCoral)
      }

      state.errorMessage?.let { message ->
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
      }

      state.rawTaskListsJson?.let { jsonText ->
        Spacer(modifier = Modifier.height(24.dp))
        Text(
          text = jsonText,
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
          modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState()),
        )
      }
    }
  }
}

@Composable
private fun GoogleSignInButton(onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
  Button(
    onClick = onClick,
    enabled = enabled,
    shape = RoundedCornerShape(28.dp),
    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral, contentColor = Color.White),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    modifier = modifier.fillMaxWidth().height(56.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
        Image(painter = painterResource(R.drawable.ic_google_logo), contentDescription = null, modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.width(12.dp))
      Text(text = "Continue with Google", style = MaterialTheme.typography.labelLarge)
    }
  }
}
