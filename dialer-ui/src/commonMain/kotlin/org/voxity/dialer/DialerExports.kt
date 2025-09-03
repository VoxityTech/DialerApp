package org.voxity.dialer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.voxity.dialer.components.DialerModalSheet
import org.voxity.dialer.domain.models.CallHistoryItem
import org.voxity.dialer.domain.models.CallState
import org.voxity.dialer.domain.models.Contact
import org.voxity.dialer.presentation.ActiveCallScreen
import org.voxity.dialer.presentation.CallHistoryScreen
import org.voxity.dialer.presentation.ContactsScreen
import org.voxity.dialer.presentation.DialerScreen
import org.voxity.dialer.ui.callbacks.ActiveCallScreenCallbacks
import org.voxity.dialer.ui.callbacks.CallHistoryScreenCallbacks
import org.voxity.dialer.ui.callbacks.ContactsScreenCallbacks
import org.voxity.dialer.ui.callbacks.DialerScreenCallbacks
import org.voxity.dialer.ui.state.ActiveCallScreenState
import org.voxity.dialer.ui.state.CallHistoryScreenState
import org.voxity.dialer.ui.state.ContactsScreenState
import org.voxity.dialer.ui.state.DialerScreenState

object DialerScreens {

    @Composable
    fun Contacts(
        state: ContactsScreenState,
        callbacks: ContactsScreenCallbacks,
        modifier: Modifier = Modifier
    ) {
        ContactsScreen(state, callbacks, modifier)
    }

    @Composable
    fun CallHistory(
        state: CallHistoryScreenState,
        callbacks: CallHistoryScreenCallbacks,
        modifier: Modifier = Modifier
    ) {
        CallHistoryScreen(state, callbacks, modifier)
    }

    @Composable
    fun ActiveCall(
        state: ActiveCallScreenState,
        callbacks: ActiveCallScreenCallbacks,
        onSaveContact: (String, String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        ActiveCallScreen(state, callbacks, onSaveContact, modifier)
    }

    @Composable
    fun Keypad(
        state: DialerScreenState,
        callbacks: DialerScreenCallbacks,
        modifier: Modifier = Modifier
    ) {
        DialerScreen(state, callbacks, modifier)
    }

    @Composable
    fun Modal(
        isVisible: Boolean,
        onDismiss: () -> Unit,
        onCall: (String) -> Unit,
        initialPhoneNumber: String,
        modifier: Modifier = Modifier
    ) {
        DialerModalSheet(isVisible, onDismiss, onCall, initialPhoneNumber, modifier)
    }
}

// State factory functions
object DialerState {
    fun contacts(
        contacts: List<Contact> = emptyList(),
        isLoading: Boolean = false,
        searchQuery: String = "",
        filteredContacts: List<Contact> = emptyList()
    ) = ContactsScreenState(contacts, isLoading, searchQuery, filteredContacts)

    fun callHistory(
        callHistory: List<CallHistoryItem> = emptyList(),
        isLoading: Boolean = false
    ) = CallHistoryScreenState(callHistory, isLoading)

    fun activeCall(
        callState: CallState = CallState(),
        callDuration: Long = 0L,
        showAudioSelector: Boolean = false
    ) = ActiveCallScreenState(callState, callDuration, showAudioSelector)

    fun keypad(
        phoneNumber: String = "",
        canMakeCall: Boolean = false
    ) = DialerScreenState(phoneNumber, canMakeCall)
}

// Callback factory functions
object DialerCallbacks {
    fun contacts(
        onContactSelected: (Contact) -> Unit = {},
        onCallContact: (String) -> Unit = {},
        onSearchQueryChanged: (String) -> Unit = {},
        onSaveContact: (String,String) -> Unit = { _, _ -> },
        onBlockNumber: (String) -> Unit = {},
        onUnblockNumber: (String) -> Unit = {}
    ) = object : ContactsScreenCallbacks {
        override fun onContactSelected(contact: Contact) = onContactSelected(contact)
        override fun onCallContact(phoneNumber: String) = onCallContact(phoneNumber)
        override fun onSearchQueryChanged(query: String) = onSearchQueryChanged(query)
        override fun onBlockNumber(phoneNumber: String) = onBlockNumber(phoneNumber)
        override fun onUnblockNumber(phoneNumber: String) = onUnblockNumber(phoneNumber)
        override fun onSaveContact(contactName: String, phoneNumber: String) = onSaveContact(contactName,phoneNumber)
    }

    fun callHistory(
        onCallHistoryItemClicked: (String) -> Unit = {},
        onRefresh: () -> Unit = {},
        onSaveContact: (String, String) -> Unit = { _, _ -> }
    ) = object : CallHistoryScreenCallbacks {
        override fun onCallHistoryItemClicked(phoneNumber: String) = onCallHistoryItemClicked(phoneNumber)
        override fun onRefresh() = onRefresh()
        override fun onSaveContact(contactName: String, phoneNumber: String) = onSaveContact(contactName, phoneNumber)
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
    ) = object : ActiveCallScreenCallbacks {
        override fun onAnswerCall() = onAnswerCall()
        override fun onRejectCall() = onRejectCall()
        override fun onEndCall() = onEndCall()
        override fun onHoldCall() = onHoldCall()
        override fun onUnholdCall() = onUnholdCall()
        override fun onMuteCall(muted: Boolean) = onMuteCall(muted)
        override fun onShowAudioSelector() = onShowAudioSelector()
        override fun onHideAudioSelector() = onHideAudioSelector()
        override fun onAddCall() = Unit
        override fun onMergeCall() = Unit
    }

    fun keypad(
        onNumberChanged: (String) -> Unit = {},
        onMakeCall: (String) -> Unit = {},
        onDeleteDigit: () -> Unit = {},
        onClearNumber: () -> Unit = {}
    ) = object : DialerScreenCallbacks {
        override fun onNumberChanged(number: String) = onNumberChanged(number)
        override fun onMakeCall(phoneNumber: String) = onMakeCall(phoneNumber)
        override fun onDeleteDigit() = onDeleteDigit()
        override fun onClearNumber() = onClearNumber()
    }
}