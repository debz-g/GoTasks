package com.debzg.gotasks.di

import com.debzg.gotasks.presentation.signin.SignInViewModel
import com.debzg.gotasks.presentation.tasks.TasksViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
  viewModel { SignInViewModel(get(), get(), get(), get(), get(), get()) }
  viewModel { TasksViewModel(get(), get(), get()) }
}
