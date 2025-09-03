package org.voxity.dialer.ui.di

import org.voxity.dialer.ui.viewmodel.DialerViewModel
import org.koin.dsl.module

val uiModule = module {
    factory { DialerViewModel(get(), get()) }
}