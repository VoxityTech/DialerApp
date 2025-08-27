// src/commonMain/kotlin/voxity/org/dialer/domain/repository/CallRepository.kt
package voxity.org.dialer.domain.repository

import kotlinx.coroutines.flow.StateFlow
import voxity.org.dialer.domain.models.CallState

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