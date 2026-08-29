package com.debzg.gotasks.data.mapper

import com.debzg.gotasks.data.local.entity.TaskEntity
import com.debzg.gotasks.data.local.entity.TaskListEntity
import com.debzg.gotasks.data.remote.dto.TaskDto
import com.debzg.gotasks.data.remote.dto.TaskListDto
import com.debzg.gotasks.domain.model.Task
import com.debzg.gotasks.domain.model.TaskList
import java.time.Instant

const val LOCAL_ID_PREFIX = "local_"

fun String.isLocalId(): Boolean = startsWith(LOCAL_ID_PREFIX)

private fun String.toEpochMillisOrNull(): Long? = runCatching { Instant.parse(this).toEpochMilli() }.getOrNull()

/**
 * Converts a server task into a row, carrying over every local-only field from [cached].
 *
 * Those fields (star, reminder time) have no Tasks API equivalent, so the response says nothing
 * about them — without merging the cached row they'd silently reset on every pull. Taking the whole
 * cached entity rather than one parameter per field means a new local-only column is preserved by
 * default instead of being quietly dropped until someone notices.
 */
fun TaskDto.toEntity(taskListId: String, cached: TaskEntity? = null): TaskEntity =
  TaskEntity(
    id = requireNotNull(id) { "Task response missing id" },
    taskListId = taskListId,
    title = title.orEmpty(),
    notes = notes,
    status = status ?: "needsAction",
    due = due?.toEpochMillisOrNull(),
    completed = completed?.toEpochMillisOrNull(),
    deleted = deleted ?: false,
    hidden = hidden ?: false,
    parent = parent,
    position = position.orEmpty(),
    etag = etag,
    updated = updated?.toEpochMillisOrNull() ?: 0L,
    selfLink = selfLink,
    webViewLink = webViewLink,
    isStarred = cached?.isStarred ?: false,
    localReminderTime = cached?.localReminderTime,
    isReminderSet = cached?.isReminderSet ?: false,
  )

fun TaskEntity.toDomain(): Task =
  Task(
    id = id,
    taskListId = taskListId,
    title = title,
    notes = notes,
    isCompleted = status == "completed",
    due = due?.let { Instant.ofEpochMilli(it) },
    completedAt = completed?.let { Instant.ofEpochMilli(it) },
    parentId = parent,
    position = position,
    isStarred = isStarred,
    reminderTime = localReminderTime?.let { Instant.ofEpochMilli(it) },
  )

fun TaskListDto.toEntity(): TaskListEntity =
  TaskListEntity(
    id = requireNotNull(id) { "TaskList response missing id" },
    title = title.orEmpty(),
    etag = etag,
    updated = updated?.toEpochMillisOrNull() ?: 0L,
    selfLink = selfLink,
  )

fun TaskListEntity.toDomain(): TaskList = TaskList(id = id, title = title)
