// src/commonMain/kotlin/voxity/org/dialer/presentation/viewmodel/DialerViewModel.kt
package voxity.org.dialer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import voxity.org.dialer.domain.usecases.CallUseCases
import voxity.org.dialer.domain.models.CallHistoryItem
import voxity.org.dialer.domain.models.Contact

class DialerViewModel(
    private val callUseCases: CallUseCases
) : ViewModel() {

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
        loadContacts()
        loadCallHistory()
    }

    fun loadContacts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
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
            try {
                _isLoading.value = true
                _error.value = null
                _callHistory.value = callUseCases.getCallHistory()
            } catch (e: Exception) {
                _error.value = "Failed to load call history: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Non-suspend call operations
    fun makeCall(phoneNumber: String) {
        try {
            callUseCases.makeCall(phoneNumber)
        } catch (e: Exception) {
            _error.value = "Failed to make call: ${e.message}"
        }
    }

    fun answerCall() {
        try {
            callUseCases.answerCall()
        } catch (e: Exception) {
            _error.value = "Failed to answer call: ${e.message}"
        }
    }

    fun rejectCall() {
        try {
            callUseCases.rejectCall()
        } catch (e: Exception) {
            _error.value = "Failed to reject call: ${e.message}"
        }
    }

    fun endCall() {
        try {
            callUseCases.endCall()
        } catch (e: Exception) {
            _error.value = "Failed to end call: ${e.message}"
        }
    }

    fun holdCall() {
        try {
            callUseCases.holdCall()
        } catch (e: Exception) {
            _error.value = "Failed to hold call: ${e.message}"
        }
    }

    fun unholdCall() {
        try {
            callUseCases.unholdCall()
        } catch (e: Exception) {
            _error.value = "Failed to unhold call: ${e.message}"
        }
    }

    fun muteCall(muted: Boolean) {
        try {
            callUseCases.muteCall(muted)
        } catch (e: Exception) {
            _error.value = "Failed to ${if (muted) "mute" else "unmute"} call: ${e.message}"
        }
    }

    fun blockNumber(phoneNumber: String) {
        try {
            callUseCases.blockNumber(phoneNumber)
        } catch (e: Exception) {
            _error.value = "Failed to block number: ${e.message}"
        }
    }

    fun unblockNumber(phoneNumber: String) {
        try {
            callUseCases.unblockNumber(phoneNumber)
        } catch (e: Exception) {
            _error.value = "Failed to unblock number: ${e.message}"
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun refresh() {
        loadContacts()
        loadCallHistory()
    }
}