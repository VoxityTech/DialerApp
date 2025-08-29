package io.voxity.dialer

import io.voxity.dialer.domain.models.CallResult

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect suspend fun makeCall(phoneNumber: String): CallResult
expect suspend fun endCall(): CallResult
expect suspend fun answerCall(): CallResult
expect suspend fun rejectCall(): CallResult