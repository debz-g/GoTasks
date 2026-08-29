package com.debzg.gotasks.di

import com.debzg.gotasks.data.auth.AuthInterceptor
import com.debzg.gotasks.data.auth.AuthStateRepository
import com.debzg.gotasks.data.auth.AuthTokenHolder
import com.debzg.gotasks.data.auth.CredentialAuthService
import com.debzg.gotasks.data.auth.SessionRestorer
import com.debzg.gotasks.data.auth.TasksAuthenticator
import com.debzg.gotasks.data.auth.TasksAuthorizationClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val authModule = module {
  single { AuthTokenHolder() }
  single { AuthStateRepository() }
  single { CredentialAuthService(androidContext()) }
  single { TasksAuthorizationClient(androidContext()) }
  single { AuthInterceptor(get()) }
  single { TasksAuthenticator(get(), get(), get()) }
  single { SessionRestorer(get(), get(), get()) }
}
