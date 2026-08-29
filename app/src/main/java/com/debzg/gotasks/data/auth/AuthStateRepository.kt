package com.debzg.gotasks.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthStateRepository {
  private val _state = MutableStateFlow<AuthState>(AuthState.Restoring)
  val state: StateFlow<AuthState> = _state.asStateFlow()

  fun update(newState: AuthState) {
    _state.value = newState
  }
}
