package org.voxity.dialer.ui.viewmodel

import org.voxity.dialer.domain.models.CallHistoryItem
import org.voxity.dialer.domain.models.Contact
import org.voxity.dialer.domain.usecases.CallUseCases
import org.voxity.dialer.platform.PhoneCaller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class DialerViewModel(
    private val callUseCases: CallUseCases,
    private val phoneCaller: PhoneCaller,
    private val defaultDispatcher: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val viewModelScope = defaultDispatcher

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _callHistory = MutableStateFlow<List<CallHistoryItem>>(emptyList())
    val callHistory: StateFlow<List<CallHistoryItem>> = _callHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    val callState = callUseCases.callState

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        loadContacts()
        loadCallHistory()
    }

    fun loadContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _contacts.value = callUseCases.getContacts()
            } catch (e: Exception) {
                _error.value = "Failed to load contacts: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCallHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _callHistory.value = callUseCases.getCallHistory()
            } catch (e: Exception) {
                _error.value = "Failed to load call history: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun makeCall(phoneNumber: String) {
        try {
            _error.value = null
            phoneCaller.initiateCall(phoneNumber)
        } catch (e: Exception) {
            _error.value = "Failed to initiate call: ${e.message}"
        }
    }

    fun answerCall() {
        viewModelScope.launch {
            try {
                _error.value = null
                callUseCases.answerCall()
            } catch (e: Exception) {
                _error.value = "Failed to answer call: ${e.message}"
            }
        }
    }

    fun rejectCall() {
        viewModelScope.launch {
            try {
                _error.value = null
                callUseCases.rejectCall()
            } catch (e: Exception) {
                _error.value = "Failed to reject call: ${e.message}"
            }
        }
    }

    fun endCall() {
        viewModelScope.launch {
            try {
                _error.value = null
                callUseCases.endCall()
            } catch (e: Exception) {
                _error.value = "Failed to end call: ${e.message}"
            }
        }
    }

    fun holdCall() {
        viewModelScope.launch {
            try {
                _error.value = null
                callUseCases.holdCall()
            } catch (e: Exception) {
                _error.value = "Failed to hold call: ${e.message}"
            }
        }
    }

    fun unholdCall() {
        viewModelScope.launch {
            try {
                _error.value = null
                callUseCases.unholdCall()
            } catch (e: Exception) {
                _error.value = "Failed to unhold call: ${e.message}"
            }
        }
    }

    fun muteCall(muted: Boolean) {
        viewModelScope.launch {
            try {
                _error.value = null
                callUseCases.muteCall(muted)
            } catch (e: Exception) {
                _error.value = "Failed to ${if (muted) "mute" else "unmute"} call: ${e.message}"
            }
        }
    }

    fun blockNumber(phoneNumber: String) {
        viewModelScope.launch {
            try {
                _error.value = null
                callUseCases.blockNumber(phoneNumber)
            } catch (e: Exception) {
                _error.value = "Failed to block number: ${e.message}"
            }
        }
    }

    fun unblockNumber(phoneNumber: String) {
        viewModelScope.launch {
            try {
                _error.value = null
                callUseCases.unblockNumber(phoneNumber)
            } catch (e: Exception) {
                _error.value = "Failed to unblock number: ${e.message}"
            }
        }
    }


    fun clearError() {
        _error.value = null
    }

    fun refreshAllData() {
        loadInitialData()
    }

    fun onCleared() {
        viewModelScope.cancel()
    }

    fun onDialIntentReceived(phoneNumber: String) {
        // For DIAL intents, just show the number in the UI, don't auto-call
        println("DialerViewModel: Received dial intent for $phoneNumber")
        // Don't call makeCall() here - that creates the loop!
    }

    fun onCallIntentReceived(phoneNumber: String) {
        makeCall(phoneNumber)
        println("DialerViewModel: Received call intent for $phoneNumber, placing call.")
    }
}
