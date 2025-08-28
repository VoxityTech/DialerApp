package io.voxity.dialer.domain.usecases

import io.voxity.dialer.domain.repository.CallHistoryRepository
import io.voxity.dialer.domain.repository.CallRepository
import io.voxity.dialer.domain.repository.ContactRepository

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

    suspend fun getContacts() = contactRepository.getContacts()
    suspend fun getCallHistory() = callHistoryRepository.getCallHistory()
}