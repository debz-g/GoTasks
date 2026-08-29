package com.debzg.gotasks

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.debzg.gotasks.data.sync.SyncScheduler
import com.debzg.gotasks.di.authModule
import com.debzg.gotasks.di.dataModule
import com.debzg.gotasks.di.syncModule
import com.debzg.gotasks.di.viewModelModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class GoTasksApp : Application() {

  private val syncScheduler: SyncScheduler by inject()

  override fun onCreate() {
    super.onCreate()

    startKoin {
      androidLogger()
      androidContext(this@GoTasksApp)
      modules(authModule, dataModule, syncModule, viewModelModule)
    }

    syncScheduler.schedulePeriodicSync()
    ProcessLifecycleOwner.get().lifecycle.addObserver(ForegroundSyncObserver(syncScheduler))
  }
}

/** Kicks off a sync whenever the app returns to the foreground. */
private class ForegroundSyncObserver(private val syncScheduler: SyncScheduler) : DefaultLifecycleObserver {
  override fun onStart(owner: LifecycleOwner) {
    syncScheduler.scheduleForegroundSync()
  }
}
