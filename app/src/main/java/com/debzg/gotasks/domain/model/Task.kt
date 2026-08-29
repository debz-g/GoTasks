package com.debzg.gotasks.domain.model

import java.time.Instant

data class Task(
  val id: String,
  val taskListId: String,
  val title: String,
  val notes: String?,
  val isCompleted: Boolean,
  val due: Instant?,
  val completedAt: Instant?,
  val parentId: String?,
  val position: String,
  val isStarred: Boolean = false,
)
