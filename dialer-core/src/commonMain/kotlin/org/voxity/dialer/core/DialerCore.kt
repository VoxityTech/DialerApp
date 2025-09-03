package org.voxity.dialer.core

import org.voxity.dialer.domain.models.DialerConfig
import org.voxity.dialer.domain.models.CallResult

interface DialerCore {
    suspend fun initialize(config: DialerConfig): CallResult
    suspend fun shutdown(): CallResult
    fun isInitialized(): Boolean
}