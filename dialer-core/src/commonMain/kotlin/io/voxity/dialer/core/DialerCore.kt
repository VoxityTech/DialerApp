package io.voxity.dialer.core

import io.voxity.dialer.domain.models.DialerConfig
import io.voxity.dialer.domain.models.CallResult

interface DialerCore {
    suspend fun initialize(config: DialerConfig): CallResult
    suspend fun shutdown(): CallResult
    fun isInitialized(): Boolean
}