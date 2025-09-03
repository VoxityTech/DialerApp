package org.voxity.dialer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.voxity.dialer.domain.models.Contact
import org.voxity.dialer.presentation.DialerMainUI
import org.voxity.dialer.ui.callbacks.ActiveCallScreenCallbacks
import org.voxity.dialer.ui.callbacks.CallHistoryScreenCallbacks
import org.voxity.dialer.ui.callbacks.ContactsScreenCallbacks
import org.voxity.dialer.ui.callbacks.DialerNavigationCallbacks
import org.voxity.dialer.ui.callbacks.DialerScreenCallbacks
import org.voxity.dialer.ui.navigation.NavigationItem
import org.voxity.dialer.ui.navigation.NavigationScreenRenderer
import org.voxity.dialer.ui.state.ActiveCallScreenState
import org.voxity.dialer.ui.state.CallHistoryScreenState
import org.voxity.dialer.ui.state.ContactsScreenState
import org.voxity.dialer.ui.state.DialerNavigationState
import org.voxity.dialer.ui.state.DialerScreenState

// Re-export the DialerMainUI function from presentation layer
@Composable
fun DialerMainUI(
    navigationState: DialerNavigationState,
    contactsState: ContactsScreenState = ContactsScreenState(),
    callHistoryState: CallHistoryScreenState = CallHistoryScreenState(),
    activeCallState: ActiveCallScreenState = ActiveCallScreenState(),
    dialerState: DialerScreenState = DialerScreenState(),

    navigationCallbacks: DialerNavigationCallbacks,
    contactsCallbacks: ContactsScreenCallbacks = object : ContactsScreenCallbacks {
        override fun onContactSelected(contact: Contact) {}
        override fun onCallContact(phoneNumber: String) {}
        override fun onSearchQueryChanged(query: String) {}
        override fun onSaveContact(contactName: String, phoneNumber: String) {}
    },
    callHistoryCallbacks: CallHistoryScreenCallbacks = object : CallHistoryScreenCallbacks {
        override fun onCallHistoryItemClicked(phoneNumber: String) {}
        override fun onSaveContact(contactName: String, phoneNumber: String) {}
    },
    activeCallCallbacks: ActiveCallScreenCallbacks = object : ActiveCallScreenCallbacks {
        override fun onAnswerCall() {}
        override fun onRejectCall() {}
        override fun onEndCall() {}
        override fun onHoldCall() {}
        override fun onUnholdCall() {}
        override fun onMuteCall(muted: Boolean) {}
        override fun onShowAudioSelector() {}
        override fun onHideAudioSelector() {}
        override fun onAddCall() {}
        override fun onMergeCall() {}
    },
    dialerCallbacks: DialerScreenCallbacks = object : DialerScreenCallbacks {
        override fun onNumberChanged(number: String) {}
        override fun onMakeCall(phoneNumber: String) {}
        override fun onDeleteDigit() {}
        override fun onClearNumber() {}
    },

    additionalNavigationItems: List<NavigationItem> = emptyList(),
    customScreenRenderer: NavigationScreenRenderer? = null,
    modifier: Modifier = Modifier
) {
    // Delegate to the actual implementation
    DialerMainUI(
        navigationState = navigationState,
        contactsState = contactsState,
        callHistoryState = callHistoryState,
        activeCallState = activeCallState,
        dialerState = dialerState,
        navigationCallbacks = navigationCallbacks,
        contactsCallbacks = contactsCallbacks,
        callHistoryCallbacks = callHistoryCallbacks,
        activeCallCallbacks = activeCallCallbacks,
        dialerCallbacks = dialerCallbacks,
        additionalNavigationItems = additionalNavigationItems,
        customScreenRenderer = customScreenRenderer,
        modifier = modifier
    )
}