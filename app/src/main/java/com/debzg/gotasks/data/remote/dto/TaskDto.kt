package com.debzg.gotasks.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
  val id: String? = null,
  val etag: String? = null,
  val title: String? = null,
  val updated: String? = null,
  val selfLink: String? = null,
  val parent: String? = null,
  val position: String? = null,
  val notes: String? = null,
  val status: String? = null, // "needsAction" | "completed"
  val due: String? = null,
  val completed: String? = null,
  val deleted: Boolean? = null,
  val hidden: Boolean? = null,
  val webViewLink: String? = null,
)

@Serializable
data class TasksResponseDto(val etag: String? = null, val nextPageToken: String? = null, val items: List<TaskDto> = emptyList())
