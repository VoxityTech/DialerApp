package org.voxity.dialer.domain.usecases

import org.voxity.dialer.domain.repository.CallHistoryRepository
import org.voxity.dialer.domain.repository.CallRepository
import org.voxity.dialer.domain.repository.ContactRepository
import org.voxity.dialer.domain.models.CallResult
import org.voxity.dialer.domain.models.DialerResult

class CallUseCases(
    private val callRepository: CallRepository,
    private val contactRepository: ContactRepository,
    private val callHistoryRepository: CallHistoryRepository
) {
    suspend fun makeCall(phoneNumber: String): CallResult = callRepository.makeCall(phoneNumber)
    suspend fun answerCall(): CallResult = callRepository.answerCall()
    suspend fun endCall(): CallResult = callRepository.endCall()
    suspend fun rejectCall(): CallResult = callRepository.rejectCall()
    suspend fun holdCall(): CallResult = callRepository.holdCall()
    suspend fun unholdCall(): CallResult = callRepository.unholdCall()
    suspend fun muteCall(muted: Boolean): CallResult = callRepository.muteCall(muted)
    suspend fun blockNumber(phoneNumber: String): DialerResult<Boolean> = callRepository.blockNumber(phoneNumber)
    suspend fun unblockNumber(phoneNumber: String): DialerResult<Boolean> = callRepository.unblockNumber(phoneNumber)
    suspend fun isNumberBlocked(phoneNumber: String): DialerResult<Boolean> = callRepository.isNumberBlocked(phoneNumber)
    suspend fun getBlockedNumbers(): DialerResult<List<String>> = callRepository.getBlockedNumbers()

    val callState = callRepository.currentCallState

    suspend fun getContacts() = contactRepository.getContacts()
    suspend fun getCallHistory() = callHistoryRepository.getCallHistory()

    suspend fun saveContact(name: String, phoneNumber: String): CallResult =
        contactRepository.saveContact(name, phoneNumber)
}