package com.debzg.gotasks.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TaskListDto(
  val id: String? = null,
  val etag: String? = null,
  val title: String? = null,
  val updated: String? = null,
  val selfLink: String? = null,
)

@Serializable
data class TaskListsResponseDto(val etag: String? = null, val nextPageToken: String? = null, val items: List<TaskListDto> = emptyList())
