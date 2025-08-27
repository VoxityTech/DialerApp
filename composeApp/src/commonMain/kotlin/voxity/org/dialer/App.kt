package voxity.org.dialer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import voxity.org.dialer.domain.usecases.CallUseCases
import voxity.org.dialer.presentation.CallHistoryScreen
import voxity.org.dialer.presentation.ContactsScreen
import voxity.org.dialer.presentation.components.DialerPopup


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    onRequestDefaultDialer: () -> Unit = {},
    onRequestPermissions: () -> Unit = {},
    isDefaultDialer: Boolean = false,
    callUseCases: CallUseCases
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showDialer by remember { mutableStateOf(false) }

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
            when (selectedTab) {
                0 -> CallHistoryScreen(
                    modifier = Modifier.padding(paddingValues),
                    callUseCases = callUseCases
                )
                1 -> ContactsScreen(
                    modifier = Modifier.padding(paddingValues),
                    callUseCases = callUseCases
                )
            }

            if (showDialer) {
                DialerPopup(
                    onDismiss = { showDialer = false },
                    onCall = { number ->
                        callUseCases.makeCall(number)
                        showDialer = false
                    }
                )
            }
        }
    }
}