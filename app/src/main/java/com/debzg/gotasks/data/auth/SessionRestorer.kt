package com.debzg.gotasks.data.auth

import android.util.Log

/**
 * Attempts a silent re-authorization at app start.
 *
 * The access token itself is deliberately never persisted (see [AuthTokenHolder]) — instead, as
 * long as the user's consent for the Tasks scope still stands, Play Services hands back a fresh
 * token with no UI. So a cold start only needs to show the sign-in screen when consent is actually
 * missing or was revoked.
 */
class SessionRestorer(
  private val tasksAuthorizationClient: TasksAuthorizationClient,
  private val authTokenHolder: AuthTokenHolder,
  private val authStateRepository: AuthStateRepository,
) {
  suspend fun restore() {
    try {
      val result = tasksAuthorizationClient.authorize()
      val token = result.accessToken
      if (result.hasResolution() || token == null) {
        // Consent needed (or never granted) — this requires UI, so fall back to the sign-in screen.
        authStateRepository.update(AuthState.SignedOut)
        return
      }
      authTokenHolder.accessToken = token
      authStateRepository.update(AuthState.Authorized(email = null))
    } catch (e: Exception) {
      Log.w(TAG, "Silent authorization failed; falling back to sign-in", e)
      authStateRepository.update(AuthState.SignedOut)
    }
  }

  private companion object {
    const val TAG = "SessionRestorer"
  }
}
