package com.debzg.gotasks.data.sync

/**
 * Orchestrates a sync run. Push runs before pull (pull lands in step 6) so local changes reach the
 * server before any server state is folded back in.
 */
class SyncEngine(private val pushSyncStage: PushSyncStage) {
  suspend fun sync(): PushOutcome = pushSyncStage.push()
}
