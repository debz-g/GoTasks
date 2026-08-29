package com.debzg.gotasks.data.remote

import com.debzg.gotasks.data.remote.dto.TaskDto
import com.debzg.gotasks.data.remote.dto.TaskListDto
import com.debzg.gotasks.data.remote.dto.TaskListsResponseDto
import com.debzg.gotasks.data.remote.dto.TaskUpdateDto
import com.debzg.gotasks.data.remote.dto.TasksResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Thin Retrofit mirror of the Google Tasks REST API (https://tasks.googleapis.com/tasks/v1/). */
interface TasksApiService {

  @GET("users/@me/lists")
  suspend fun getTaskLists(@Query("maxResults") maxResults: Int = 100, @Query("pageToken") pageToken: String? = null): TaskListsResponseDto

  @POST("users/@me/lists") suspend fun createTaskList(@Body body: TaskListDto): TaskListDto

  @PATCH("users/@me/lists/{taskListId}")
  suspend fun updateTaskList(@Path("taskListId") taskListId: String, @Body body: TaskListDto): TaskListDto

  @DELETE("users/@me/lists/{taskListId}") suspend fun deleteTaskList(@Path("taskListId") taskListId: String)

  @GET("lists/{taskListId}/tasks")
  suspend fun getTasks(
    @Path("taskListId") taskListId: String,
    @Query("updatedMin") updatedMin: String? = null,
    @Query("showDeleted") showDeleted: Boolean = true,
    @Query("showHidden") showHidden: Boolean = true,
    @Query("showCompleted") showCompleted: Boolean = true,
    @Query("maxResults") maxResults: Int = 100,
    @Query("pageToken") pageToken: String? = null,
  ): TasksResponseDto

  @POST("lists/{taskListId}/tasks")
  suspend fun createTask(
    @Path("taskListId") taskListId: String,
    @Body body: TaskDto,
    @Query("parent") parent: String? = null,
    @Query("previous") previous: String? = null,
  ): TaskDto

  @PATCH("lists/{taskListId}/tasks/{taskId}")
  suspend fun updateTask(@Path("taskListId") taskListId: String, @Path("taskId") taskId: String, @Body body: TaskUpdateDto): TaskDto

  @DELETE("lists/{taskListId}/tasks/{taskId}")
  suspend fun deleteTask(@Path("taskListId") taskListId: String, @Path("taskId") taskId: String)

  @POST("lists/{taskListId}/tasks/{taskId}/move")
  suspend fun moveTask(
    @Path("taskListId") taskListId: String,
    @Path("taskId") taskId: String,
    @Query("parent") parent: String? = null,
    @Query("previous") previous: String? = null,
    @Query("destinationTasklist") destinationTaskList: String? = null,
  ): TaskDto
}
