package org.voxity.dialer.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.voxity.dialer.components.DialerModalSheet
import org.voxity.dialer.ui.navigation.NavigationItem
import org.voxity.dialer.ui.navigation.NavigationScreenRenderer
import org.voxity.dialer.domain.models.Contact
import org.voxity.dialer.ui.callbacks.ActiveCallScreenCallbacks
import org.voxity.dialer.ui.callbacks.CallHistoryScreenCallbacks
import org.voxity.dialer.ui.callbacks.ContactsScreenCallbacks
import org.voxity.dialer.ui.callbacks.DialerNavigationCallbacks
import org.voxity.dialer.ui.callbacks.DialerScreenCallbacks
import org.voxity.dialer.ui.state.ActiveCallScreenState
import org.voxity.dialer.ui.state.CallHistoryScreenState
import org.voxity.dialer.ui.state.ContactsScreenState
import org.voxity.dialer.ui.state.DefaultScreens
import org.voxity.dialer.ui.state.DialerNavigationState
import org.voxity.dialer.ui.state.DialerScreenState

@OptIn(ExperimentalMaterial3Api::class)
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
    val showInCallScreen = activeCallState.callState.isActive ||
            activeCallState.callState.isRinging ||
            activeCallState.callState.isConnecting ||
            activeCallState.callState.isOnHold

    val defaultNavigationItems = listOfNotNull(
        if (navigationState.showHistory) NavigationItem(
            DefaultScreens.CallHistory,
            "History",
            Icons.Default.History,
            "History"
        ) else null,
        if (navigationState.showContacts) NavigationItem(
            DefaultScreens.Contacts,
            "Contacts",
            Icons.Default.Contacts,
            "Contacts"
        ) else null
    )

    val allNavigationItems = defaultNavigationItems + additionalNavigationItems

    LaunchedEffect(navigationState.selectedTab) {
        if (navigationState.showDialerModal) {
            navigationCallbacks.onHideDialerModal()
        }
    }

    if (showInCallScreen) {
        ActiveCallScreen(
            state = activeCallState,
            callbacks = activeCallCallbacks,
            onSaveContact = contactsCallbacks::onSaveContact,
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
                        val currentScreen = allNavigationItems.find { it.id == navigationState.selectedTab }

                        if (currentScreen != null) {
                            currentScreen.render(Modifier.fillMaxSize())
                        } else {
                            customScreenRenderer?.RenderScreen(
                                screenId = navigationState.selectedTab,
                                modifier = Modifier.fillMaxSize()
                            ) ?: Box(
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
                    initialPhoneNumber = dialerState.phoneNumber,
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