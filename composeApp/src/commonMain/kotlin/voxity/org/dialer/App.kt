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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import voxity.org.dialer.presentation.CallHistoryScreen
import voxity.org.dialer.presentation.ContactsScreen
import voxity.org.dialer.presentation.InCallScreen
import voxity.org.dialer.presentation.SearchScreen
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
    var showSearchScreen by remember { mutableStateOf(false) }

    val contacts by viewModel.contacts.collectAsState()
    val callHistory by viewModel.callHistory.collectAsState()
    val callState by viewModel.callState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Handle error messages
    LaunchedEffect(error) {
        error?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = it,
                    duration = SnackbarDuration.Long
                )
                viewModel.clearError()
            }
        }
    }

    // Determine if InCallScreen should be shown
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
            // Main UI when not in a call
            MaterialTheme { // Apply your app's theme
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        AppTopBar(
                            isDefaultDialer = isDefaultDialer,
                            onRequestDefaultDialer = onRequestDefaultDialer,
                            onSearchClick = { showSearchScreen = true },
                            // Optional: Add a refresh button
                            onRefreshClick = { viewModel.refreshAllData() }
                        )
                    },
                    bottomBar = {
                        AppBottomNavigationBar(
                            currentScreen = selectedTab,
                            onScreenSelected = { selectedTab = it }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { showDialerPopup = true },
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(Icons.Default.Dialpad, contentDescription = "Open Dialer")
                        }
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                        if (showSearchScreen) {
                            SearchScreen(
                                contacts = contacts,
                                callHistory = callHistory,
                                onBack = { showSearchScreen = false },
                                onCallClick = { phoneNumber ->
                                    viewModel.makeCall(phoneNumber)
                                    showSearchScreen = false
                                }
                            )
                        } else {
                            when (selectedTab) {
                                Screen.CallHistory -> CallHistoryScreen(
                                    callHistory = callHistory,
                                    onCallClick = viewModel::makeCall
                                )

                                Screen.Contacts -> ContactsScreen(
                                    contacts = contacts,
                                    onCallClick = viewModel::makeCall,
                                    onBlockNumber = viewModel::blockNumber,
                                    onUnblockNumber = viewModel::unblockNumber
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

                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize().align(Alignment.Center),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class Screen {
    CallHistory,
    Contacts
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    isDefaultDialer: Boolean,
    onRequestDefaultDialer: () -> Unit,
    onSearchClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Column {
        if (!isDefaultDialer) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "⚠️ Not Default Dialer",
                        color = Color.Red,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Set as default dialer to make and receive calls properly.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                    Button(onClick = onRequestDefaultDialer) {
                        Text("Set as Default")
                    }
                }
            }
        }
        TopAppBar(
            title = { Text("Dialer") },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
                IconButton(onClick = onRefreshClick) { // Refresh button
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Data")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun AppBottomNavigationBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.History, contentDescription = "Call History") },
            label = { Text("History") },
            selected = currentScreen == Screen.CallHistory,
            onClick = { onScreenSelected(Screen.CallHistory) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Contacts, contentDescription = "Contacts") },
            label = { Text("Contacts") },
            selected = currentScreen == Screen.Contacts,
            onClick = { onScreenSelected(Screen.Contacts) }
        )
    }
}