package io.voxity.dialer.navigation

// Navigation destination identifiers that host apps can use
object DialerDestinations {
    const val CONTACTS = "dialer_contacts"
    const val CALL_HISTORY = "dialer_call_history"
    const val KEYPAD = "dialer_keypad"
    const val ACTIVE_CALL = "dialer_active_call"
    const val DIALER_FEATURE = "dialer_feature" // Complete feature
}

// Helper for hosts using Jetpack Navigation
// This would be in androidMain if using Jetpack Navigation
/*
fun NavGraphBuilder.dialerDestinations(
    dialerViewModel: YourDialerViewModel // Host's ViewModel
) {
    composable(DialerDestinations.CONTACTS) {
        ContactsScreen(
            state = dialerViewModel.contactsState.collectAsState().value,
            callbacks = dialerViewModel.createContactsCallbacks()
        )
    }

    composable(DialerDestinations.CALL_HISTORY) {
        CallHistoryScreen(
            state = dialerViewModel.callHistoryState.collectAsState().value,
            callbacks = dialerViewModel.createCallHistoryCallbacks()
        )
    }

    composable(DialerDestinations.KEYPAD) {
        DialerScreen(
            state = dialerViewModel.dialerState.collectAsState().value,
            callbacks = dialerViewModel.createDialerCallbacks()
        )
    }

    composable(DialerDestinations.ACTIVE_CALL) {
        ActiveCallScreen(
            state = dialerViewModel.activeCallState.collectAsState().value,
            callbacks = dialerViewModel.createActiveCallCallbacks()
        )
    }
}
*/