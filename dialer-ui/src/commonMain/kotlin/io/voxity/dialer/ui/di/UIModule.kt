package io.voxity.dialer.ui.di

import io.voxity.dialer.ui.viewmodel.DialerViewModel
import org.koin.dsl.module

val uiModule = module {
    factory { DialerViewModel(get(), get()) }
}