// src/commonMain/kotlin/voxity/org/dialer/App.kt
package voxity.org.dialer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import voxity.org.dialer.presentation.CallHistoryScreen
import voxity.org.dialer.presentation.ContactsScreen
import voxity.org.dialer.presentation.InCallScreen
import voxity.org.dialer.presentation.components.DialerBottomSheet
import voxity.org.dialer.presentation.viewmodel.DialerViewModel
import voxity.org.dialer.ui.theme.DialerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    viewModel: DialerViewModel,
    onRequestDefaultDialer: () -> Unit,
    onRequestPermissions: () -> Unit,
    isDefaultDialer: Boolean
) {
    var selectedTab by remember { mutableStateOf(Screen.CallHistory) }
    var showDialerPopup by remember { mutableStateOf(false) }

    val contacts by viewModel.contacts.collectAsState()
    val callHistory by viewModel.callHistory.collectAsState()
    val callState by viewModel.callState.collectAsState()

    val showInCallScreen = callState.isActive || callState.isRinging ||
            callState.isConnecting || callState.isOnHold

    DialerTheme {
        if (showInCallScreen) {
            InCallScreen(
                callState = callState,
                onAnswerCall = viewModel::answerCall,
                onRejectCall = viewModel::rejectCall,
                onEndCall = viewModel::endCall,
                onHoldCall = viewModel::holdCall,
                onUnholdCall = viewModel::unholdCall,
                onMuteCall = viewModel::muteCall
            )
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.History, contentDescription = "History") },
                            label = { Text("History") },
                            selected = selectedTab == Screen.CallHistory,
                            onClick = { selectedTab = Screen.CallHistory }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Contacts, contentDescription = "Contacts") },
                            label = { Text("Contacts") },
                            selected = selectedTab == Screen.Contacts,
                            onClick = { selectedTab = Screen.Contacts }
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showDialerPopup = true },
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f), // Slight transparency like other elements
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
                    // Default dialer warning (if needed)
                    if (!isDefaultDialer) {
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

                    when (selectedTab) {
                        Screen.CallHistory -> CallHistoryScreen(
                            callHistory = callHistory,
                            onCallClick = viewModel::makeCall
                        )
                        Screen.Contacts -> ContactsScreen(
                            contacts = contacts,
                            onCallClick = viewModel::makeCall
                        )
                    }
                }

                if (showDialerPopup) {
                    DialerBottomSheet(
                        isVisible = showDialerPopup,
                        onDismiss = { showDialerPopup = false },
                        onCall = { number ->
                            viewModel.makeCall(number)
                            showDialerPopup = false
                        }
                    )
                }
            }
        }
    }
}

enum class Screen {
    CallHistory,
    Contacts
}
