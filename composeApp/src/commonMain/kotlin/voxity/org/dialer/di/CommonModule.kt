// src/commonMain/kotlin/voxity/org/dialer/di/CommonModule.kt
package voxity.org.dialer.di

import org.koin.dsl.module
import voxity.org.dialer.domain.usecases.CallUseCases
import voxity.org.dialer.presentation.viewmodel.DialerViewModel

val commonModule = module {
    factory { CallUseCases(get(), get(), get()) }
    factory { DialerViewModel(get()) }
}