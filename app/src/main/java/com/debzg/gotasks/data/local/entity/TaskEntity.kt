package com.debzg.gotasks.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "tasks",
  // onUpdate CASCADE so reconciling a temp local_ list id to its server id automatically
  // re-points every task in that list, instead of orphaning them.
  foreignKeys =
    [ForeignKey(TaskListEntity::class, ["id"], ["taskListId"], onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)],
  indices = [Index("taskListId"), Index("parent"), Index("updated")],
)
data class TaskEntity(
  @PrimaryKey val id: String,
  val taskListId: String,
  val title: String,
  val notes: String?,
  val status: String, // "needsAction" | "completed"
  val due: Long?,
  val completed: Long?,
  val deleted: Boolean,
  val hidden: Boolean,
  val parent: String?,
  val position: String,
  val etag: String?,
  val updated: Long,
  val selfLink: String?,
  val webViewLink: String?,
  val localReminderTime: Long? = null,
  val isReminderSet: Boolean = false,
  val isStarred: Boolean = false, // local-only, not part of the Google Tasks API schema
  val syncState: SyncState = SyncState.SYNCED,
)
