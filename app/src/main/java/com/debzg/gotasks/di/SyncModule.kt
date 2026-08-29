package com.debzg.gotasks.di

import com.debzg.gotasks.data.sync.PushSyncStage
import com.debzg.gotasks.data.sync.SyncEngine
import com.debzg.gotasks.data.sync.SyncScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val syncModule = module {
  single { SyncScheduler(androidContext()) }
  single { PushSyncStage(get(), get(), get(), get(), get(), get(), get()) }
  single { SyncEngine(get()) }
}
