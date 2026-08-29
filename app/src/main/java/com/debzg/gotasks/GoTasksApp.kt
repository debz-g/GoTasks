package com.debzg.gotasks

import android.app.Application
import com.debzg.gotasks.di.authModule
import com.debzg.gotasks.di.dataModule
import com.debzg.gotasks.di.syncModule
import com.debzg.gotasks.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class GoTasksApp : Application() {
  override fun onCreate() {
    super.onCreate()

    startKoin {
      androidLogger()
      androidContext(this@GoTasksApp)
      modules(authModule, dataModule, syncModule, viewModelModule)
    }
  }
}
