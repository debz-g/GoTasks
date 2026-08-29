package com.debzg.gotasks.data.auth

/** In-memory only — the access token is never persisted to disk. */
class AuthTokenHolder {
  @Volatile var accessToken: String? = null
}
