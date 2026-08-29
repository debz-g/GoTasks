package com.debzg.gotasks.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.debzg.gotasks.data.local.entity.SyncMetadataEntity

@Dao
interface SyncMetadataDao {
  @Query("SELECT metaValue FROM sync_metadata WHERE metaKey = :key") suspend fun get(key: String): String?

  @Upsert suspend fun upsert(entity: SyncMetadataEntity)

  suspend fun put(key: String, value: String) = upsert(SyncMetadataEntity(key, value))
}
