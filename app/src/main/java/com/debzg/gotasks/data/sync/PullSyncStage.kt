package com.debzg.gotasks.data.sync

import android.util.Log
import com.debzg.gotasks.data.auth.AuthState
import com.debzg.gotasks.data.auth.AuthStateRepository
import com.debzg.gotasks.data.local.dao.PendingOperationDao
import com.debzg.gotasks.data.local.dao.SyncMetadataDao
import com.debzg.gotasks.data.local.dao.TaskDao
import com.debzg.gotasks.data.local.dao.TaskListDao
import com.debzg.gotasks.data.mapper.isLocalId
import com.debzg.gotasks.data.mapper.toEntity
import com.debzg.gotasks.data.remote.TasksApiService
import com.debzg.gotasks.data.remote.dto.TaskListDto
import java.io.IOException
import java.time.Instant
import retrofit2.HttpException

private const val KEY_LAST_PULL = "last_pull_timestamp"

/**
 * Clock-skew cushion subtracted from the pull watermark. Without it, a task updated server-side
 * moments after a pull started could fall just outside the next `updatedMin` window and be missed.
 */
private const val PULL_WATERMARK_SKEW_MILLIS = 60_000L

/**
 * Folds server state back into the local cache.
 *
 * Task lists are always pulled in full (there are only a handful, and it's the only way to notice
 * ones deleted elsewhere). Tasks use the API's `updatedMin` for incremental pulls after the first
 * full sync.
 */
class PullSyncStage(
  private val taskDao: TaskDao,
  private val taskListDao: TaskListDao,
  private val pendingOperationDao: PendingOperationDao,
  private val syncMetadataDao: SyncMetadataDao,
  private val api: TasksApiService,
  private val authStateRepository: AuthStateRepository,
) {

  suspend fun pull(): SyncOutcome {
    // Stamp the watermark from before the request goes out, so anything changed *during* the pull
    // is still caught next time.
    val startedAtMillis = System.currentTimeMillis()
    val lastPull = syncMetadataDao.get(KEY_LAST_PULL)

    return try {
      // Anything with unpushed local edits is left alone — the local version wins until it's pushed.
      val dirtyIds = pendingOperationDao.getAllPendingEntityIds().toSet()

      val serverLists = fetchAllTaskLists()
      reconcileTaskLists(serverLists, dirtyIds)

      for (list in serverLists) {
        val listId = list.id ?: continue
        pullTasksForList(listId, updatedMin = lastPull, dirtyIds = dirtyIds)
      }

      syncMetadataDao.put(KEY_LAST_PULL, Instant.ofEpochMilli(startedAtMillis - PULL_WATERMARK_SKEW_MILLIS).toString())
      SyncOutcome.Success
    } catch (e: HttpException) {
      if (e.code() == 401 || e.code() == 403) {
        Log.w(TAG, "Authorization required during pull", e)
        authStateRepository.update(AuthState.NeedsReauthorization)
        SyncOutcome.AuthRequired
      } else {
        Log.w(TAG, "Pull failed (HTTP ${e.code()})", e)
        SyncOutcome.Retry
      }
    } catch (e: IOException) {
      Log.w(TAG, "Pull failed: network unavailable", e)
      SyncOutcome.Retry
    }
  }

  private suspend fun fetchAllTaskLists(): List<TaskListDto> {
    val all = mutableListOf<TaskListDto>()
    var pageToken: String? = null
    do {
      val page = api.getTaskLists(pageToken = pageToken)
      all += page.items
      pageToken = page.nextPageToken
    } while (pageToken != null)
    return all
  }

  private suspend fun reconcileTaskLists(serverLists: List<TaskListDto>, dirtyIds: Set<String>) {
    taskListDao.upsertAll(serverLists.filter { it.id !in dirtyIds }.map { it.toEntity() })

    // Drop lists that no longer exist server-side — but never touch ones created locally that
    // haven't been pushed yet, or any list with edits still queued.
    val serverIds = serverLists.mapNotNull { it.id }.toSet()
    taskListDao
      .getAll()
      .filter { it.id !in serverIds && !it.id.isLocalId() && it.id !in dirtyIds }
      .forEach { stale ->
        Log.d(TAG, "Removing list deleted elsewhere: ${stale.id}")
        taskListDao.deleteById(stale.id) // cascades to its tasks
      }
  }

  private suspend fun pullTasksForList(taskListId: String, updatedMin: String?, dirtyIds: Set<String>) {
    var pageToken: String? = null
    do {
      val page =
        api.getTasks(
          taskListId = taskListId,
          updatedMin = updatedMin,
          showDeleted = true,
          showHidden = true,
          showCompleted = true,
          pageToken = pageToken,
        )

      val entities =
        page.items
          .filter { it.id !in dirtyIds }
          .map { dto -> dto.toEntity(taskListId, cached = dto.id?.let { taskDao.getById(it) }) }
      if (entities.isNotEmpty()) taskDao.upsertAll(entities)

      pageToken = page.nextPageToken
    } while (pageToken != null)
  }

  private companion object {
    const val TAG = "PullSyncStage"
  }
}
