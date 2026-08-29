package com.debzg.gotasks.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * PATCH body for a task. Only the fields present are changed, so absent ones are left as they are
 * server-side — that's what keeps a partial edit from clobbering a change made on another device.
 *
 * [due] is a [JsonElement] rather than a `String?` for a specific reason: the Json instance is
 * configured with `explicitNulls = false`, so a Kotlin null would simply be omitted and the field
 * would stay unchanged. There'd be no way to express "clear this date". A [JsonNull] is a *value*,
 * not a Kotlin null, so it survives serialisation and reaches Google as an explicit `"due": null`.
 */
@Serializable
data class TaskUpdateDto(
  val title: String? = null,
  val notes: String? = null,
  val status: String? = null,
  val completed: String? = null,
  val due: JsonElement? = null,
) {
  companion object {
    /** Absent — leaves the existing due date untouched. */
    val DueUnchanged: JsonElement? = null

    /** Explicit null — clears the due date. */
    val DueCleared: JsonElement = JsonNull

    fun dueOf(rfc3339: String?): JsonElement = rfc3339?.let { JsonPrimitive(it) } ?: JsonNull
  }
}
