// src/commonMain/kotlin/voxity/org/dialer/domain/usecases/CallUseCases.kt
package voxity.org.dialer.domain.usecases

import voxity.org.dialer.domain.repository.CallRepository
import voxity.org.dialer.domain.repository.ContactRepository
import voxity.org.dialer.domain.repository.CallHistoryRepository

class CallUseCases(
    private val callRepository: CallRepository,
    private val contactRepository: ContactRepository,
    private val callHistoryRepository: CallHistoryRepository
) {
    fun makeCall(phoneNumber: String) = callRepository.makeCall(phoneNumber)
    fun answerCall() = callRepository.answerCall()
    fun endCall() = callRepository.endCall()
    fun rejectCall() = callRepository.rejectCall()
    fun holdCall() = callRepository.holdCall()
    fun unholdCall() = callRepository.unholdCall()
    fun muteCall(muted: Boolean) = callRepository.muteCall(muted)
    fun blockNumber(phoneNumber: String) = callRepository.blockNumber(phoneNumber)
    fun unblockNumber(phoneNumber: String) = callRepository.unblockNumber(phoneNumber)
    fun isNumberBlocked(phoneNumber: String) = callRepository.isNumberBlocked(phoneNumber)
    fun getBlockedNumbers() = callRepository.getBlockedNumbers()

    val callState = callRepository.currentCallState

    // These remain suspend as they involve data access
    suspend fun getContacts() = contactRepository.getContacts()
    suspend fun getCallHistory() = callHistoryRepository.getCallHistory()
}