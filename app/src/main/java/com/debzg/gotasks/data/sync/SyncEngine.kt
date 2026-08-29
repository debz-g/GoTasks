package com.debzg.gotasks.data.sync

import android.util.Log

/**
 * Runs a full sync pass: push first, then pull.
 *
 * That order matters — pushing first means local changes reach the server before server state is
 * folded back in, so a pull can't overwrite an edit that was about to be sent anyway.
 */
class SyncEngine(private val pushSyncStage: PushSyncStage, private val pullSyncStage: PullSyncStage) {

  suspend fun sync(): SyncOutcome {
    val pushOutcome = pushSyncStage.push()
    if (pushOutcome != SyncOutcome.Success) {
      // Auth is broken or the network is down — a pull would just fail the same way.
      Log.d(TAG, "Skipping pull; push returned $pushOutcome")
      return pushOutcome
    }
    return pullSyncStage.pull()
  }

  private companion object {
    const val TAG = "SyncEngine"
  }
}
