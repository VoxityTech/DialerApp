package org.voxity.dialer.di

import org.voxity.dialer.domain.usecases.CallUseCases
import org.koin.dsl.module

val commonModule = module {
    factory { CallUseCases(get(), get(), get()) }
}