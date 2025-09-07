package org.voxity.dialer.domain.repository

import org.voxity.dialer.domain.models.CallState
import org.voxity.dialer.domain.models.CallResult
import org.voxity.dialer.domain.models.DialerResult
import kotlinx.coroutines.flow.StateFlow

interface CallRepository {
    val currentCallState: StateFlow<CallState>

    suspend fun makeCall(phoneNumber: String): CallResult
    suspend fun answerCall(): CallResult
    suspend fun rejectCall(): CallResult
    suspend fun endCall(): CallResult
    suspend fun holdCall(): CallResult
    suspend fun unholdCall(): CallResult
    suspend fun muteCall(muted: Boolean): CallResult
    suspend fun blockNumber(phoneNumber: String): DialerResult<Boolean>
    suspend fun unblockNumber(phoneNumber: String): DialerResult<Boolean>
    suspend fun isNumberBlocked(phoneNumber: String): DialerResult<Boolean>
    suspend fun getBlockedNumbers(): DialerResult<List<String>>

    fun onCallAdded(call: Any)
    fun onCallRemoved(call: Any)
    fun onCallStateChanged()
    fun onCallDetailsChanged()
    fun silenceRinger()
}