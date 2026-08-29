package com.debzg.gotasks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Small key/value table for sync bookkeeping (currently just the incremental-pull watermark). */
@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(@PrimaryKey val metaKey: String, val metaValue: String)
