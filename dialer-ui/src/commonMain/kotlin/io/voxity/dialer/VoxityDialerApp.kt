package io.voxity.dialer


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.voxity.dialer.presentation.CallHistoryScreen
import io.voxity.dialer.presentation.ContactsScreen
import io.voxity.dialer.presentation.ActiveCallView
import io.voxity.dialer.presentation.components.DialerModalSheet
import io.voxity.dialer.ui.viewmodel.DialerViewModel
import io.voxity.dialer.ui.theme.DialerTheme

data class NavigationItem(
    val id: Any,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoxityDialerApp(
    viewModel: DialerViewModel,
    onRequestDefaultDialer: () -> Unit,
    onRequestPermissions: () -> Unit,
    isDefaultDialer: Boolean,
    additionalNavigationItems: List<NavigationItem> = emptyList(),
    onRenderAdditionalScreen: @Composable (screenIdentifier: Any, paddingValues: PaddingValues) -> Unit = { _, _ -> }
) {
    var selectedTab by remember { mutableStateOf<Any>(Screen.CallHistory) }
    var showDialerPopup by remember { mutableStateOf(false) }

    val contacts by viewModel.contacts.collectAsState()
    val callHistory by viewModel.callHistory.collectAsState()
    val callState by viewModel.callState.collectAsState()

    val showInCallScreen = callState.isActive || callState.isRinging ||
            callState.isConnecting || callState.isOnHold

    val defaultNavigationItems = listOf(
        NavigationItem(Screen.CallHistory, "History", Icons.Default.History, "History"),
        NavigationItem(Screen.Contacts, "Contacts", Icons.Default.Contacts, "Contacts")
    )

    val allNavigationItems = defaultNavigationItems + additionalNavigationItems

    DialerTheme {
        if (showInCallScreen) {
            ActiveCallView(
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
                        allNavigationItems.forEach { item ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        item.icon,
                                        contentDescription = item.contentDescription
                                    )
                                },
                                label = { Text(item.label) },
                                selected = selectedTab == item.id,
                                onClick = { selectedTab = item.id }
                            )
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showDialerPopup = true },
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

                        else -> {
                            // Check if the selected tab is one of the additional items
                            if (additionalNavigationItems.any { it.id == selectedTab }) {
                                onRenderAdditionalScreen(selectedTab, paddingValues)
                            } else {
                                // Fallback or error state if needed, though with current logic
                                // selectedTab should always be one of the allNavigationItems
                                Text("Unknown screen", modifier = Modifier.padding(16.dp))
                            }
                        }
                    }
                }

                if (showDialerPopup) {
                    DialerModalSheet(
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
