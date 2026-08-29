package com.debzg.gotasks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_lists")
data class TaskListEntity(
  @PrimaryKey val id: String,
  val title: String,
  val etag: String?,
  val updated: Long,
  val selfLink: String?,
)
