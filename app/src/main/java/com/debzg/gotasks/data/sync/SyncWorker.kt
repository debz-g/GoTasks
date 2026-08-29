package com.debzg.gotasks.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.debzg.gotasks.data.local.dao.PendingOperationDao
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
  private val pendingOperationDao: PendingOperationDao by inject()
  private val syncScheduler: SyncScheduler by inject()

  override suspend fun doWork(): Result {
    val result =
      when (syncEngine.sync()) {
        SyncOutcome.Success -> {
          // A mutation made after the drain's final queue check wouldn't have enqueued its own run
          // (pushes use KEEP while this worker is alive), so pick it up here rather than leaving it
          // until the next foreground or periodic sync.
          if (pendingOperationDao.getNextPending(listOf(-1L)) != null) {
            Log.d(TAG, "Work queued during sync; scheduling another push")
            syncScheduler.schedulePush()
          }
          Result.success()
        }
        SyncOutcome.Retry -> Result.retry()
        // Needs user interaction — retrying on a backoff would just burn battery until they act.
        SyncOutcome.AuthRequired -> {
          Log.w(TAG, "Sync paused: re-authorization required")
          Result.success()
        }
      }
    Log.d(TAG, "Sync finished: $result")
    return result
  }

  companion object {
    private const val TAG = "SyncWorker"
    const val UNIQUE_WORK_PUSH = "sync-push"
    const val UNIQUE_WORK_FOREGROUND = "sync-foreground"
    const val UNIQUE_WORK_PERIODIC = "sync-periodic"
  }
}
