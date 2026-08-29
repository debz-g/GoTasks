package com.debzg.gotasks.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class SyncScheduler(context: Context) {
  private val workManager = WorkManager.getInstance(context)

  /**
   * Queues a push after a local mutation.
   *
   * REPLACE + a short initial delay debounces bursts of edits (typing, rapid checkbox taps) into a
   * single run instead of one per keystroke.
   */
  fun schedulePush() {
    val request =
      OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .setInitialDelay(PUSH_DEBOUNCE_SECONDS, TimeUnit.SECONDS)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        .build()

    workManager.enqueueUniqueWork(SyncWorker.UNIQUE_WORK_PUSH, ExistingWorkPolicy.REPLACE, request)
  }

  private companion object {
    const val PUSH_DEBOUNCE_SECONDS = 2L
  }
}
