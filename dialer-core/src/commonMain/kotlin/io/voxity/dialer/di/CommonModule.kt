package io.voxity.dialer.di

import io.voxity.dialer.domain.usecases.CallUseCases
import org.koin.dsl.module

val commonModule = module {
    factory { CallUseCases(get(), get(), get()) }
}