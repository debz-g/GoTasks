package com.debzg.gotasks.data.auth

import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Silently retries an authorization grant on 401. Runs on OkHttp's background dispatcher, so
 * blocking here is safe. If the grant needs UI resolution (or fails), gives up and flips
 * [AuthStateRepository] to [AuthState.NeedsReauthorization] for the UI to handle.
 */
class TasksAuthenticator(
  private val tasksAuthorizationClient: TasksAuthorizationClient,
  private val authTokenHolder: AuthTokenHolder,
  private val authStateRepository: AuthStateRepository,
) : Authenticator {
  override fun authenticate(route: Route?, response: Response): Request? {
    if (responseCount(response) >= 2) return null

    val refreshedToken =
      try {
        val result = runBlocking { tasksAuthorizationClient.authorize() }
        if (result.hasResolution()) null else result.accessToken
      } catch (e: ApiException) {
        null
      }

    if (refreshedToken == null) {
      authStateRepository.update(AuthState.NeedsReauthorization)
      return null
    }

    authTokenHolder.accessToken = refreshedToken
    return response.request.newBuilder().header("Authorization", "Bearer $refreshedToken").build()
  }

  private fun responseCount(response: Response): Int {
    var count = 1
    var prior = response.priorResponse
    while (prior != null) {
      count++
      prior = prior.priorResponse
    }
    return count
  }
}
