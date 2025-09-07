package org.voxity.dialer.ui

import org.voxity.dialer.ui.di.uiModule
import org.koin.core.module.Module
import org.voxity.dialer.DialerCallbacks
import org.voxity.dialer.DialerScreens
import org.voxity.dialer.DialerState

object DialerUI {
    val commonUiModule: List<Module> = listOf(uiModule)

    val State = DialerState
    val Callbacks = DialerCallbacks
    val Screens = DialerScreens
}