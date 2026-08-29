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
   * KEEP, deliberately not REPLACE: REPLACE *cancels a running worker*, and a push cancelled
   * between "POST /tasks succeeded" and "outbox row deleted" leaves the CREATE queued even though
   * the server already has the task — the next run then creates a duplicate. KEEP never interrupts
   * an in-flight drain.
   *
   * Nothing is lost by not enqueuing: a pending run hasn't started yet and will pick the new op up,
   * and a running drain re-queries the queue each iteration. [SyncWorker] re-schedules if anything
   * is still queued when it finishes, covering the narrow window after its final query.
   *
   * The short delay still debounces bursts of edits into a single run.
   */
  fun schedulePush() {
    val request =
      OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(networkConstraint)
        .setInitialDelay(PUSH_DEBOUNCE_SECONDS, TimeUnit.SECONDS)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        .build()

    workManager.enqueueUniqueWork(SyncWorker.UNIQUE_WORK_PUSH, ExistingWorkPolicy.KEEP, request)
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
