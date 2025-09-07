package org.voxity.dialer.features

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.voxity.dialer.components.DialerModalSheet
import org.voxity.dialer.presentation.DialerScreen
import org.voxity.dialer.ui.callbacks.ActiveCallScreenCallbacks
import org.voxity.dialer.ui.callbacks.CallHistoryScreenCallbacks
import org.voxity.dialer.ui.callbacks.ContactsScreenCallbacks
import org.voxity.dialer.ui.callbacks.DialerScreenCallbacks
import org.voxity.dialer.ui.state.ActiveCallScreenState
import org.voxity.dialer.ui.state.CallHistoryScreenState
import org.voxity.dialer.ui.state.ContactsScreenState
import org.voxity.dialer.ui.state.DialerScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialerFeature(
    // Navigation callbacks - host controls where to go
    onNavigateToCallHistory: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToActiveCall: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToCustomScreen: (screenId: String) -> Unit = {},

    // Feature state
    contactsState: ContactsScreenState = ContactsScreenState(),
    callHistoryState: CallHistoryScreenState = CallHistoryScreenState(),
    activeCallState: ActiveCallScreenState = ActiveCallScreenState(),
    dialerState: DialerScreenState = DialerScreenState(),

    // Feature callbacks
    contactsCallbacks: ContactsScreenCallbacks,
    callHistoryCallbacks: CallHistoryScreenCallbacks,
    activeCallCallbacks: ActiveCallScreenCallbacks,
    dialerCallbacks: DialerScreenCallbacks,

    // Configuration
    showDefaultDialerPrompt: Boolean = false,
    onRequestDefaultDialer: () -> Unit = {},

    modifier: Modifier = Modifier
) {
    var showDialerModal by remember { mutableStateOf(false) }

    // Check if we should show in-call screen
    val showInCallScreen = activeCallState.callState.isActive ||
            activeCallState.callState.isRinging ||
            activeCallState.callState.isConnecting ||
            activeCallState.callState.isOnHold

    // If in call, navigate to call screen and let host handle it
    LaunchedEffect(showInCallScreen) {
        if (showInCallScreen) {
            onNavigateToActiveCall()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (showDefaultDialerPrompt) {
            DefaultDialerPrompt(onRequestDefaultDialer = onRequestDefaultDialer)
        }

        // This would typically be just one screen content
        // Host navigation determines which screen to show
        Box(modifier = Modifier.weight(1f)) {
            // Content would be determined by host navigation
            // This is just an example - host should handle screen switching
        }

        // Floating dialer button
        Box(modifier = Modifier.fillMaxWidth()) {
            FloatingActionButton(
                onClick = { showDialerModal = true },
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(16.dp)
                    .size(64.dp)
            ) {
                Icon(
                    Icons.Default.Dialpad,
                    contentDescription = "Dialer",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    if (showDialerModal) {
        DialerModalSheet(
            isVisible = showDialerModal,
            onDismiss = { showDialerModal = false },
            onCall = { number ->
                dialerCallbacks.onMakeCall(number)
                showDialerModal = false
            }
        )
    }
}

// Simpler feature for just the dialer functionality
@Composable
fun DialerKeypadFeature(
    state: DialerScreenState,
    callbacks: DialerScreenCallbacks,
    onRequestDefaultDialer: () -> Unit = {},
    showDefaultDialerPrompt: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (showDefaultDialerPrompt) {
            DefaultDialerPrompt(onRequestDefaultDialer = onRequestDefaultDialer)
        }

        DialerScreen(
            state = state,
            callbacks = callbacks,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DefaultDialerPrompt(
    onRequestDefaultDialer: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Set as default dialer",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error
            )
            Button(
                onClick = onRequestDefaultDialer,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Enable")
            }
        }
    }
}