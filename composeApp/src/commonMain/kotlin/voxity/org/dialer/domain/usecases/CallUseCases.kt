// src/commonMain/kotlin/voxity/org/dialer/domain/usecases/CallUseCases.kt
package voxity.org.dialer.domain.usecases

import voxity.org.dialer.domain.repository.CallRepository

class CallUseCases(private val repository: CallRepository) {
    fun makeCall(phoneNumber: String) = repository.makeCall(phoneNumber)
    fun answerCall() = repository.answerCall()
    fun endCall() = repository.endCall()
    fun rejectCall() = repository.rejectCall()
    fun holdCall() = repository.holdCall()
    fun unholdCall() = repository.unholdCall()
    fun muteCall(muted: Boolean) = repository.muteCall(muted)
    val callState = repository.currentCallState
}