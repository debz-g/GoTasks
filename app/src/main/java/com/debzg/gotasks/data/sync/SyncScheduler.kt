package com.debzg.gotasks.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class SyncScheduler(context: Context) {
  private val workManager = WorkManager.getInstance(context)

  private val networkConstraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

  /**
   * Queues a sync after a local mutation.
   *
   * REPLACE + a short initial delay debounces bursts of edits (typing, rapid checkbox taps) into a
   * single run instead of one per keystroke.
   */
  fun schedulePush() {
    val request =
      OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(networkConstraint)
        .setInitialDelay(PUSH_DEBOUNCE_SECONDS, TimeUnit.SECONDS)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        .build()

    workManager.enqueueUniqueWork(SyncWorker.UNIQUE_WORK_PUSH, ExistingWorkPolicy.REPLACE, request)
  }

  /**
   * Syncs when the app comes to the foreground, so changes made on another device show up without
   * waiting for the periodic run. KEEP so re-entering the app mid-sync doesn't restart it.
   */
  fun scheduleForegroundSync() {
    val request =
      OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(networkConstraint)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        .build()

    workManager.enqueueUniqueWork(SyncWorker.UNIQUE_WORK_FOREGROUND, ExistingWorkPolicy.KEEP, request)
  }

  /** Background catch-up. 15 minutes is WorkManager's minimum periodic interval. */
  fun schedulePeriodicSync() {
    val request =
      PeriodicWorkRequestBuilder<SyncWorker>(PERIODIC_INTERVAL_MINUTES, TimeUnit.MINUTES)
        .setConstraints(networkConstraint)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        .build()

    workManager.enqueueUniquePeriodicWork(SyncWorker.UNIQUE_WORK_PERIODIC, ExistingPeriodicWorkPolicy.KEEP, request)
  }

  private companion object {
    const val PUSH_DEBOUNCE_SECONDS = 2L
    const val PERIODIC_INTERVAL_MINUTES = 15L
  }
}
