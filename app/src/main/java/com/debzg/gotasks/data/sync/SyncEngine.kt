package com.debzg.gotasks.data.sync

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Runs a full sync pass: push first, then pull.
 *
 * That order matters — pushing first means local changes reach the server before server state is
 * folded back in, so a pull can't overwrite an edit that was about to be sent.
 */
class SyncEngine(private val pushSyncStage: PushSyncStage, private val pullSyncStage: PullSyncStage) {

  /**
   * Serialises every sync in the process.
   *
   * Syncs start from two independent places — [SyncWorker] and the Refresh button, which calls
   * straight through without touching WorkManager — so WorkManager's unique-work policy alone
   * can't keep them apart. Run concurrently, both drains read the same pending CREATE and both
   * POST it, creating the task twice on the server.
   *
   * The second caller waits rather than bailing out, so a manual refresh still reflects whatever
   * the in-flight run pulled down. It then finds an empty queue and simply re-pulls.
   */
  private val syncMutex = Mutex()

  suspend fun sync(): SyncOutcome =
    syncMutex.withLock {
      val pushOutcome = pushSyncStage.push()
      if (pushOutcome != SyncOutcome.Success) {
        // Auth is broken or the network is down — a pull would just fail the same way.
        Log.d(TAG, "Skipping pull; push returned $pushOutcome")
        return@withLock pushOutcome
      }
      pullSyncStage.pull()
    }

  private companion object {
    const val TAG = "SyncEngine"
  }
}
