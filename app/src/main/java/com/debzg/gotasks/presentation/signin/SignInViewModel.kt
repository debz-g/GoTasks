package com.debzg.gotasks.presentation.signin

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debzg.gotasks.BuildConfig
import com.debzg.gotasks.data.auth.AuthState
import com.debzg.gotasks.data.auth.AuthStateRepository
import com.debzg.gotasks.data.auth.AuthTokenHolder
import com.debzg.gotasks.data.auth.CredentialAuthService
import com.debzg.gotasks.data.auth.TasksAuthorizationClient
import com.debzg.gotasks.data.remote.TasksApiService
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SignInViewModel(
  private val credentialAuthService: CredentialAuthService,
  private val tasksAuthorizationClient: TasksAuthorizationClient,
  private val authTokenHolder: AuthTokenHolder,
  private val authStateRepository: AuthStateRepository,
  private val tasksApiService: TasksApiService,
  private val json: Json,
) : ViewModel() {

  private val _state = MutableStateFlow(SignInState())
  val state: StateFlow<SignInState> = _state.asStateFlow()

  fun onIntent(intent: SignInIntent, activity: Activity) {
    when (intent) {
      is SignInIntent.SignIn -> signIn(activity)
      is SignInIntent.AuthorizationResolved -> handleAuthorizationResolved(intent.resultData)
      is SignInIntent.AuthorizationIntentSenderConsumed -> _state.update { it.copy(pendingAuthorizationIntentSender = null) }
      is SignInIntent.FetchTaskLists -> fetchTaskLists()
    }
  }

  private fun signIn(activity: Activity) {
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true, errorMessage = null) }
      try {
        val credential = credentialAuthService.signIn(activity, BuildConfig.WEB_CLIENT_ID)
        _state.update { it.copy(signedInEmail = credential.id) }
        requestTasksAuthorization()
      } catch (e: Exception) {
        _state.update { it.copy(isLoading = false, errorMessage = e.message ?: "Sign-in failed") }
      }
    }
  }

  private suspend fun requestTasksAuthorization() {
    try {
      val result = tasksAuthorizationClient.authorize()
      if (result.hasResolution()) {
        _state.update { it.copy(isLoading = false, pendingAuthorizationIntentSender = result.pendingIntent?.intentSender) }
      } else {
        authTokenHolder.accessToken = result.accessToken
        authStateRepository.update(AuthState.Authorized(_state.value.signedInEmail))
        _state.update { it.copy(isLoading = false) }
      }
    } catch (e: ApiException) {
      _state.update { it.copy(isLoading = false, errorMessage = "Authorization failed: ${e.message}") }
    }
  }

  private fun handleAuthorizationResolved(resultData: Intent?) {
    if (resultData == null) {
      _state.update { it.copy(isLoading = false, errorMessage = "Authorization was cancelled") }
      return
    }
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true) }
      try {
        val result = tasksAuthorizationClient.resultFromIntent(resultData)
        authTokenHolder.accessToken = result.accessToken
        authStateRepository.update(AuthState.Authorized(_state.value.signedInEmail))
        _state.update { it.copy(isLoading = false) }
      } catch (e: ApiException) {
        _state.update { it.copy(isLoading = false, errorMessage = "Authorization failed: ${e.message}") }
      }
    }
  }

  private fun fetchTaskLists() {
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true, errorMessage = null) }
      try {
        val response = tasksApiService.getTaskLists()
        _state.update { it.copy(isLoading = false, rawTaskListsJson = json.encodeToString(response)) }
      } catch (e: Exception) {
        _state.update { it.copy(isLoading = false, errorMessage = e.message ?: "Fetch failed") }
      }
    }
  }
}
