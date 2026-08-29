package com.debzg.gotasks.data.auth

import android.content.Context
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.SecureRandom

/** Wraps Credential Manager's "Sign in with Google" flow — identity only, no Tasks API access. */
class CredentialAuthService(private val context: Context) {
  private val credentialManager = CredentialManager.create(context)

  suspend fun signIn(activityContext: Context, webClientId: String): GoogleIdTokenCredential {
    val option = GetSignInWithGoogleOption.Builder(serverClientId = webClientId).setNonce(generateNonce()).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
    val response = credentialManager.getCredential(request = request, context = activityContext)

    val credential = response.credential
    require(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
      "Unexpected credential type: ${credential.type}"
    }
    return GoogleIdTokenCredential.createFrom(credential.data)
  }

  suspend fun signOut() {
    credentialManager.clearCredentialState(ClearCredentialStateRequest())
  }

  private fun generateNonce(byteLength: Int = 32): String {
    val randomBytes = ByteArray(byteLength)
    SecureRandom().nextBytes(randomBytes)
    return Base64.encodeToString(randomBytes, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
  }
}
