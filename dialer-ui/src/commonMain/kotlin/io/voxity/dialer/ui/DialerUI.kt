package io.voxity.dialer.ui

import io.voxity.dialer.ui.di.uiModule
import org.koin.core.module.Module

object DialerUI {
    val modules: List<Module> = listOf(uiModule)
}