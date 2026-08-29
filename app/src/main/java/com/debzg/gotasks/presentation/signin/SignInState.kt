package com.debzg.gotasks.presentation.signin

import android.content.IntentSender

data class SignInState(
  val isLoading: Boolean = false,
  val signedInEmail: String? = null,
  val pendingAuthorizationIntentSender: IntentSender? = null,
  val rawTaskListsJson: String? = null,
  val errorMessage: String? = null,
)
