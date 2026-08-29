package com.debzg.gotasks.data.sync

enum class SyncOutcome {
  /** Everything the stage set out to do completed. */
  Success,

  /** Network unavailable / server erroring — leave state as-is and let a later run retry. */
  Retry,

  /** Consent missing or revoked; syncing is paused until the user re-authorizes. */
  AuthRequired,
}
