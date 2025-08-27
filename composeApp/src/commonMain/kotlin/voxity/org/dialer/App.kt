// src/commonMain/kotlin/voxity/org/dialer/App.kt
package voxity.org.dialer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import voxity.org.dialer.presentation.viewmodel.DialerViewModel
import voxity.org.dialer.presentation.CallHistoryScreen
import voxity.org.dialer.presentation.ContactsScreen
import voxity.org.dialer.presentation.SearchScreen
import voxity.org.dialer.presentation.InCallScreen
import voxity.org.dialer.presentation.components.DialerPopup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    onRequestDefaultDialer: () -> Unit = {},
    onRequestPermissions: () -> Unit = {},
    isDefaultDialer: Boolean = false,
    viewModel: DialerViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showDialer by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }

    val contacts by viewModel.contacts.collectAsState()
    val callHistory by viewModel.callHistory.collectAsState()
    val callState by viewModel.callState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Show in-call screen when there's an active call
    if (callState.isActive || callState.isRinging || callState.isConnecting) {
        InCallScreen(
            callState = callState,
            onAnswerCall = { viewModel.answerCall() },
            onRejectCall = { viewModel.rejectCall() },
            onEndCall = { viewModel.endCall() },
            onHoldCall = { viewModel.holdCall() },
            onUnholdCall = { viewModel.unholdCall() },
            onMuteCall = { muted -> viewModel.muteCall(muted) }
        )
        return
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                if (!isDefaultDialer) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "⚠️ Not Default Dialer",
                                color = Color.Red,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Set as default dialer to make and receive calls",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = onRequestDefaultDialer,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Set as Default Dialer")
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dialer",
                                style = MaterialTheme.typography.titleLarge
                            )
                            IconButton(onClick = { showSearch = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = "Call History") },
                        label = { Text("Call History") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Contacts, contentDescription = "Contacts") },
                        label = { Text("Contacts") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialer = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Dialpad, contentDescription = "Dialer")
                }
            }
        ) { paddingValues ->
            when {
                showSearch -> {
                    SearchScreen(
                        contacts = contacts,
                        callHistory = callHistory,
                        onBack = { showSearch = false },
                        onCallClick = { phoneNumber ->
                            viewModel.makeCall(phoneNumber)
                            showSearch = false
                        }
                    )
                }
                else -> {
                    when (selectedTab) {
                        0 -> CallHistoryScreen(
                            callHistory = callHistory,
                            onCallClick = { phoneNumber -> viewModel.makeCall(phoneNumber) },
                            modifier = Modifier.padding(paddingValues)
                        )
                        1 -> ContactsScreen(
                            contacts = contacts,
                            onCallClick = { phoneNumber -> viewModel.makeCall(phoneNumber) },
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }

            if (showDialer) {
                DialerPopup(
                    onDismiss = { showDialer = false },
                    onCall = { number ->
                        viewModel.makeCall(number)
                        showDialer = false
                    }
                )
            }

            // Show error snackbar if there's an error
            error?.let { errorMessage ->
                LaunchedEffect(errorMessage) {
                    // Show snackbar or handle error display
                    viewModel.clearError()
                }
            }

            // Show loading indicator if needed
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}