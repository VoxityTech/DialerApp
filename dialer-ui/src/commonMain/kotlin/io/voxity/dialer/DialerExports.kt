package io.voxity.dialer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Simple object with @Composable functions - no val assignments
object DialerScreens {

    @Composable
    fun Contacts(
        state: io.voxity.dialer.ui.state.ContactsScreenState,
        callbacks: io.voxity.dialer.ui.callbacks.ContactsScreenCallbacks,
        modifier: Modifier = Modifier
    ) {
        io.voxity.dialer.presentation.ContactsScreen(state, callbacks, modifier)
    }

    @Composable
    fun CallHistory(
        state: io.voxity.dialer.ui.state.CallHistoryScreenState,
        callbacks: io.voxity.dialer.ui.callbacks.CallHistoryScreenCallbacks,
        modifier: Modifier = Modifier
    ) {
        io.voxity.dialer.presentation.CallHistoryScreen(state, callbacks, modifier)
    }

    @Composable
    fun ActiveCall(
        state: io.voxity.dialer.ui.state.ActiveCallScreenState,
        callbacks: io.voxity.dialer.ui.callbacks.ActiveCallScreenCallbacks,
        modifier: Modifier = Modifier
    ) {
        io.voxity.dialer.presentation.ActiveCallScreen(state, callbacks, modifier)
    }

    @Composable
    fun Keypad(
        state: io.voxity.dialer.ui.state.DialerScreenState,
        callbacks: io.voxity.dialer.ui.callbacks.DialerScreenCallbacks,
        modifier: Modifier = Modifier
    ) {
        io.voxity.dialer.presentation.DialerScreen(state, callbacks, modifier)
    }

    @Composable
    fun Modal(
        isVisible: Boolean,
        onDismiss: () -> Unit,
        onCall: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        io.voxity.dialer.presentation.components.DialerModalSheet(isVisible, onDismiss, onCall, modifier)
    }
}

// State factory functions
object DialerState {
    fun contacts(
        contacts: List<io.voxity.dialer.domain.models.Contact> = emptyList(),
        isLoading: Boolean = false,
        searchQuery: String = "",
        filteredContacts: List<io.voxity.dialer.domain.models.Contact> = emptyList()
    ) = io.voxity.dialer.ui.state.ContactsScreenState(contacts, isLoading, searchQuery, filteredContacts)

    fun callHistory(
        callHistory: List<io.voxity.dialer.domain.models.CallHistoryItem> = emptyList(),
        isLoading: Boolean = false
    ) = io.voxity.dialer.ui.state.CallHistoryScreenState(callHistory, isLoading)

    fun activeCall(
        callState: io.voxity.dialer.domain.models.CallState = io.voxity.dialer.domain.models.CallState(),
        callDuration: Long = 0L,
        showAudioSelector: Boolean = false
    ) = io.voxity.dialer.ui.state.ActiveCallScreenState(callState, callDuration, showAudioSelector)

    fun keypad(
        phoneNumber: String = "",
        canMakeCall: Boolean = false
    ) = io.voxity.dialer.ui.state.DialerScreenState(phoneNumber, canMakeCall)
}

// Callback factory functions
object DialerCallbacks {
    fun contacts(
        onContactSelected: (io.voxity.dialer.domain.models.Contact) -> Unit = {},
        onCallContact: (String) -> Unit = {},
        onSearchQueryChanged: (String) -> Unit = {},
        onBlockNumber: (String) -> Unit = {},
        onUnblockNumber: (String) -> Unit = {}
    ) = object : io.voxity.dialer.ui.callbacks.ContactsScreenCallbacks {
        override fun onContactSelected(contact: io.voxity.dialer.domain.models.Contact) = onContactSelected(contact)
        override fun onCallContact(phoneNumber: String) = onCallContact(phoneNumber)
        override fun onSearchQueryChanged(query: String) = onSearchQueryChanged(query)
        override fun onBlockNumber(phoneNumber: String) = onBlockNumber(phoneNumber)
        override fun onUnblockNumber(phoneNumber: String) = onUnblockNumber(phoneNumber)
    }

    fun callHistory(
        onCallHistoryItemClicked: (String) -> Unit = {},
        onRefresh: () -> Unit = {}
    ) = object : io.voxity.dialer.ui.callbacks.CallHistoryScreenCallbacks {
        override fun onCallHistoryItemClicked(phoneNumber: String) = onCallHistoryItemClicked(phoneNumber)
        override fun onRefresh() = onRefresh()
    }

    fun activeCall(
        onAnswerCall: () -> Unit = {},
        onRejectCall: () -> Unit = {},
        onEndCall: () -> Unit = {},
        onHoldCall: () -> Unit = {},
        onUnholdCall: () -> Unit = {},
        onMuteCall: (Boolean) -> Unit = {},
        onShowAudioSelector: () -> Unit = {},
        onHideAudioSelector: () -> Unit = {}
    ) = object : io.voxity.dialer.ui.callbacks.ActiveCallScreenCallbacks {
        override fun onAnswerCall() = onAnswerCall()
        override fun onRejectCall() = onRejectCall()
        override fun onEndCall() = onEndCall()
        override fun onHoldCall() = onHoldCall()
        override fun onUnholdCall() = onUnholdCall()
        override fun onMuteCall(muted: Boolean) = onMuteCall(muted)
        override fun onShowAudioSelector() = onShowAudioSelector()
        override fun onHideAudioSelector() = onHideAudioSelector()
    }

    fun keypad(
        onNumberChanged: (String) -> Unit = {},
        onMakeCall: (String) -> Unit = {},
        onDeleteDigit: () -> Unit = {},
        onClearNumber: () -> Unit = {}
    ) = object : io.voxity.dialer.ui.callbacks.DialerScreenCallbacks {
        override fun onNumberChanged(number: String) = onNumberChanged(number)
        override fun onMakeCall(phoneNumber: String) = onMakeCall(phoneNumber)
        override fun onDeleteDigit() = onDeleteDigit()
        override fun onClearNumber() = onClearNumber()
    }
}