package com.debzg.gotasks.data.auth

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val authTokenHolder: AuthTokenHolder) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val original = chain.request()
    val token = authTokenHolder.accessToken ?: return chain.proceed(original)
    val authorized = original.newBuilder().header("Authorization", "Bearer $token").build()
    return chain.proceed(authorized)
  }
}
