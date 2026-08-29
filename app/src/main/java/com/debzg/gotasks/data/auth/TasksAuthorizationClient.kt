package com.debzg.gotasks.data.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.tasks.await

/** Incremental OAuth authorization for the Google Tasks scope via the Identity Authorization API. */
class TasksAuthorizationClient(context: Context) {
  private val client = Identity.getAuthorizationClient(context)

  suspend fun authorize(): AuthorizationResult {
    val request = AuthorizationRequest.builder().setRequestedScopes(listOf(Scope(TASKS_SCOPE))).build()
    return client.authorize(request).await()
  }

  fun resultFromIntent(intent: Intent): AuthorizationResult = client.getAuthorizationResultFromIntent(intent)

  companion object {
    const val TASKS_SCOPE = "https://www.googleapis.com/auth/tasks"
  }
}
