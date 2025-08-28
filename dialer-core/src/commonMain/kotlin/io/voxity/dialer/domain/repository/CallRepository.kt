package io.voxity.dialer.domain.repository

import io.voxity.dialer.domain.models.CallState
import kotlinx.coroutines.flow.StateFlow

interface CallRepository {
    val currentCallState: StateFlow<CallState>
    fun makeCall(phoneNumber: String)
    fun answerCall()
    fun rejectCall()
    fun endCall()
    fun holdCall()
    fun unholdCall()
    fun muteCall(muted: Boolean)
    fun blockNumber(phoneNumber: String): Boolean
    fun unblockNumber(phoneNumber: String): Boolean
    fun isNumberBlocked(phoneNumber: String): Boolean
    fun getBlockedNumbers(): List<String>
}