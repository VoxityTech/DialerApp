package io.voxity.dialer

// Re-export the DialerMainUI function from presentation layer
@androidx.compose.runtime.Composable
fun DialerMainUI(
    navigationState: io.voxity.dialer.ui.state.DialerNavigationState,
    contactsState: io.voxity.dialer.ui.state.ContactsScreenState = io.voxity.dialer.ui.state.ContactsScreenState(),
    callHistoryState: io.voxity.dialer.ui.state.CallHistoryScreenState = io.voxity.dialer.ui.state.CallHistoryScreenState(),
    activeCallState: io.voxity.dialer.ui.state.ActiveCallScreenState = io.voxity.dialer.ui.state.ActiveCallScreenState(),
    dialerState: io.voxity.dialer.ui.state.DialerScreenState = io.voxity.dialer.ui.state.DialerScreenState(),

    navigationCallbacks: io.voxity.dialer.ui.callbacks.DialerNavigationCallbacks,
    contactsCallbacks: io.voxity.dialer.ui.callbacks.ContactsScreenCallbacks = object : io.voxity.dialer.ui.callbacks.ContactsScreenCallbacks {
        override fun onContactSelected(contact: io.voxity.dialer.domain.models.Contact) {}
        override fun onCallContact(phoneNumber: String) {}
        override fun onSearchQueryChanged(query: String) {}
        override fun onSaveContact(contactName: String, phoneNumber: String) {}
    },
    callHistoryCallbacks: io.voxity.dialer.ui.callbacks.CallHistoryScreenCallbacks = object : io.voxity.dialer.ui.callbacks.CallHistoryScreenCallbacks {
        override fun onCallHistoryItemClicked(phoneNumber: String) {}
        override fun onSaveContact(contactName: String, phoneNumber: String) {}
    },
    activeCallCallbacks: io.voxity.dialer.ui.callbacks.ActiveCallScreenCallbacks = object : io.voxity.dialer.ui.callbacks.ActiveCallScreenCallbacks {
        override fun onAnswerCall() {}
        override fun onRejectCall() {}
        override fun onEndCall() {}
        override fun onHoldCall() {}
        override fun onUnholdCall() {}
        override fun onMuteCall(muted: Boolean) {}
        override fun onShowAudioSelector() {}
        override fun onHideAudioSelector() {}
    },
    dialerCallbacks: io.voxity.dialer.ui.callbacks.DialerScreenCallbacks = object : io.voxity.dialer.ui.callbacks.DialerScreenCallbacks {
        override fun onNumberChanged(number: String) {}
        override fun onMakeCall(phoneNumber: String) {}
        override fun onDeleteDigit() {}
        override fun onClearNumber() {}
    },

    additionalNavigationItems: List<io.voxity.dialer.ui.navigation.NavigationItem> = emptyList(),
    customScreenRenderer: io.voxity.dialer.ui.navigation.NavigationScreenRenderer? = null,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    // Delegate to the actual implementation
    io.voxity.dialer.presentation.DialerMainUI(
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