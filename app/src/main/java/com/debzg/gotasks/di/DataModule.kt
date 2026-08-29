package com.debzg.gotasks.di

import androidx.room.Room
import com.debzg.gotasks.BuildConfig
import com.debzg.gotasks.data.auth.AuthInterceptor
import com.debzg.gotasks.data.auth.TasksAuthenticator
import com.debzg.gotasks.data.local.AppDatabase
import com.debzg.gotasks.data.local.OutboxRecorder
import com.debzg.gotasks.data.remote.TasksApiService
import com.debzg.gotasks.datetime.DateTimeParser
import com.debzg.gotasks.data.repository.TaskListRepositoryImpl
import com.debzg.gotasks.data.repository.TaskRepositoryImpl
import com.debzg.gotasks.domain.repository.TaskListRepository
import com.debzg.gotasks.domain.repository.TaskRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val TASKS_API_BASE_URL = "https://tasks.googleapis.com/tasks/v1/"
private const val DATABASE_NAME = "gotasks.db"

val dataModule = module {
  single {
    Json {
      ignoreUnknownKeys = true
      explicitNulls = false
      encodeDefaults = false
    }
  }

  single {
    OkHttpClient.Builder()
      .addInterceptor(get<AuthInterceptor>())
      .authenticator(get<TasksAuthenticator>())
      .apply {
        if (BuildConfig.DEBUG) {
          addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        }
      }
      .build()
  }

  single<TasksApiService> {
    Retrofit.Builder()
      .baseUrl(TASKS_API_BASE_URL)
      .client(get())
      .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
      .build()
      .create(TasksApiService::class.java)
  }

  single {
    // Local cache only, always re-fetchable from the Tasks API — destructive migration is fine pre-release.
    Room.databaseBuilder(androidContext(), AppDatabase::class.java, DATABASE_NAME).fallbackToDestructiveMigration(dropAllTables = true).build()
  }
  single { get<AppDatabase>().taskListDao() }
  single { get<AppDatabase>().taskDao() }
  single { get<AppDatabase>().pendingOperationDao() }
  single { get<AppDatabase>().syncMetadataDao() }
  single { OutboxRecorder(get(), get(), get()) }
  single { DateTimeParser() }

  single<TaskListRepository> { TaskListRepositoryImpl(get(), get(), get()) }
  single<TaskRepository> { TaskRepositoryImpl(get(), get()) }
}
