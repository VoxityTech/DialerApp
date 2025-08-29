package io.voxity.dialer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.voxity.dialer.presentation.*
import io.voxity.dialer.presentation.components.DialerModalSheet
import io.voxity.dialer.ui.state.*
import io.voxity.dialer.ui.callbacks.*
import io.voxity.dialer.ui.navigation.NavigationItem
import io.voxity.dialer.ui.navigation.NavigationScreenRenderer

// Pure UI composable - no DI, no ViewModels
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialerUI(
    // State parameters
    navigationState: DialerNavigationState,
    contactsState: ContactsScreenState = ContactsScreenState(),
    callHistoryState: CallHistoryScreenState = CallHistoryScreenState(),
    activeCallState: ActiveCallScreenState = ActiveCallScreenState(),

    // Callback parameters
    navigationCallbacks: DialerNavigationCallbacks,
    contactsCallbacks: ContactsScreenCallbacks = object : ContactsScreenCallbacks {
        override fun onContactSelected(contact: io.voxity.dialer.domain.models.Contact) {}
        override fun onCallContact(phoneNumber: String) {}
        override fun onSearchQueryChanged(query: String) {}
    },
    callHistoryCallbacks: CallHistoryScreenCallbacks = object : CallHistoryScreenCallbacks {
        override fun onCallHistoryItemClicked(phoneNumber: String) {}
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
    },
    dialerCallbacks: DialerScreenCallbacks = object : DialerScreenCallbacks {
        override fun onNumberChanged(number: String) {}
        override fun onMakeCall(phoneNumber: String) {}
        override fun onDeleteDigit() {}
        override fun onClearNumber() {}
    },

    // Configuration parameters
    additionalNavigationItems: List<NavigationItem> = emptyList(),
    customScreenRenderer: NavigationScreenRenderer? = null,

    modifier: Modifier = Modifier
) {
    val showInCallScreen = activeCallState.callState.isActive ||
            activeCallState.callState.isRinging ||
            activeCallState.callState.isConnecting ||
            activeCallState.callState.isOnHold

    val defaultNavigationItems = listOf(
        NavigationItem(DefaultScreens.CallHistory, "History", Icons.Default.History, "History"),
        NavigationItem(DefaultScreens.Contacts, "Contacts", Icons.Default.Contacts, "Contacts")
    )

    val allNavigationItems = defaultNavigationItems + additionalNavigationItems

    if (showInCallScreen) {
        ActiveCallScreen(
            state = activeCallState,
            callbacks = activeCallCallbacks,
            modifier = modifier
        )
    } else {
        Scaffold(
            modifier = modifier,
            bottomBar = {
                NavigationBar {
                    allNavigationItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.contentDescription
                                )
                            },
                            label = { Text(item.label) },
                            selected = navigationState.selectedTab == item.id,
                            onClick = { navigationCallbacks.onTabSelected(item.id) }
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = navigationCallbacks::onShowDialerModal,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.Dialpad,
                        contentDescription = "Dialer",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                if (!navigationState.isDefaultDialer) {
                    DefaultDialerPrompt(
                        onRequestDefaultDialer = navigationCallbacks::onRequestDefaultDialer
                    )
                }

                when (navigationState.selectedTab) {
                    DefaultScreens.CallHistory -> CallHistoryScreen(
                        state = callHistoryState,
                        callbacks = callHistoryCallbacks
                    )

                    DefaultScreens.Contacts -> ContactsScreen(
                        state = contactsState,
                        callbacks = contactsCallbacks
                    )

                    else -> {
                        // Render custom screens via provided renderer
                        customScreenRenderer?.RenderScreen(
                            screenId = navigationState.selectedTab,
                            modifier = Modifier.fillMaxSize()
                        ) ?: run {
                            // Fallback for unknown screens
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Unknown screen: ${navigationState.selectedTab}")
                            }
                        }
                    }
                }
            }

            if (navigationState.showDialerModal) {
                DialerModalSheet(
                    isVisible = navigationState.showDialerModal,
                    onDismiss = navigationCallbacks::onHideDialerModal,
                    onCall = { number ->
                        dialerCallbacks.onMakeCall(number)
                        navigationCallbacks.onHideDialerModal()
                    }
                )
            }
        }
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