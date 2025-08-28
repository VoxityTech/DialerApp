package io.voxity.dialer

import org.koin.core.module.Module

expect fun initializeDialerCore(context: Any)
expect val platformModules: List<Module>