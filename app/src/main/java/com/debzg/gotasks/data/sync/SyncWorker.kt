package com.debzg.gotasks.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Runs a sync pass in the background.
 *
 * Dependencies come from Koin via [KoinComponent] rather than a custom `WorkerFactory`, so the
 * default factory can construct this worker with just (context, params).
 */
class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params), KoinComponent {

  private val syncEngine: SyncEngine by inject()

  override suspend fun doWork(): Result =
    when (syncEngine.sync()) {
      SyncOutcome.Success -> Result.success()
      SyncOutcome.Retry -> Result.retry()
      // Needs user interaction — retrying on a backoff would just burn battery until they act.
      SyncOutcome.AuthRequired -> {
        Log.w(TAG, "Sync paused: re-authorization required")
        Result.success()
      }
    }.also { Log.d(TAG, "Sync finished: $it") }

  companion object {
    private const val TAG = "SyncWorker"
    const val UNIQUE_WORK_PUSH = "sync-push"
    const val UNIQUE_WORK_FOREGROUND = "sync-foreground"
    const val UNIQUE_WORK_PERIODIC = "sync-periodic"
  }
}
