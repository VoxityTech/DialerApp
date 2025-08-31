package io.voxity.dialer.ui

import io.voxity.dialer.ui.di.uiModule
import org.koin.core.module.Module

object DialerUI {
    val commonUiModule: List<Module> = listOf(uiModule)

    val State = io.voxity.dialer.DialerState
    val Callbacks = io.voxity.dialer.DialerCallbacks
    val Screens = io.voxity.dialer.DialerScreens
}