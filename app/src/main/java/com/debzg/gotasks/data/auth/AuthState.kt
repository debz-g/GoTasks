package com.debzg.gotasks.data.auth

sealed interface AuthState {
  /** Startup: attempting a silent re-authorization before deciding which screen to show. */
  data object Restoring : AuthState

  data object SignedOut : AuthState

  data class Authorized(val email: String?) : AuthState

  data object NeedsReauthorization : AuthState
}
