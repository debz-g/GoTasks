package com.debzg.gotasks.presentation.signin

import android.content.Intent

sealed interface SignInIntent {
  data object SignIn : SignInIntent

  data class AuthorizationResolved(val resultData: Intent?) : SignInIntent

  data object AuthorizationIntentSenderConsumed : SignInIntent

  data object FetchTaskLists : SignInIntent
}
