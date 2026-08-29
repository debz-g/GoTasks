package com.debzg.gotasks.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.debzg.gotasks.data.local.dao.PendingOperationDao
import com.debzg.gotasks.data.local.dao.SyncMetadataDao
import com.debzg.gotasks.data.local.dao.TaskDao
import com.debzg.gotasks.data.local.dao.TaskListDao
import com.debzg.gotasks.data.local.entity.PendingOperationEntity
import com.debzg.gotasks.data.local.entity.SyncMetadataEntity
import com.debzg.gotasks.data.local.entity.TaskEntity
import com.debzg.gotasks.data.local.entity.TaskListEntity

@Database(
  entities = [TaskListEntity::class, TaskEntity::class, PendingOperationEntity::class, SyncMetadataEntity::class],
  version = 7,
  exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun taskListDao(): TaskListDao

  abstract fun taskDao(): TaskDao

  abstract fun pendingOperationDao(): PendingOperationDao

  abstract fun syncMetadataDao(): SyncMetadataDao
}
