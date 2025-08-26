package voxity.org.dialer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import voxity.org.dialer.screens.CallHistoryScreen
import voxity.org.dialer.screens.DialerScreen
import voxity.org.dialer.models.CallState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    onRequestDefaultDialer: () -> Unit = {},
    onRequestPermissions: () -> Unit = {},
    isDefaultDialer: Boolean = false
) {
    var selectedTab by remember { mutableStateOf(0) }

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
                        icon = { Icon(Icons.Default.Dialpad, contentDescription = "Dialer") },
                        label = { Text("Dialer") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                }
            }
        ) { paddingValues ->
            when (selectedTab) {
                0 -> DialerScreen(
                    modifier = Modifier.padding(paddingValues),
                    canMakeCall = isDefaultDialer
                )
                1 -> CallHistoryScreen(modifier = Modifier.padding(paddingValues))
                2 -> SettingsScreen(
                    modifier = Modifier.padding(paddingValues),
                    onRequestDefaultDialer = onRequestDefaultDialer,
                    onRequestPermissions = onRequestPermissions,
                    isDefaultDialer = isDefaultDialer
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    modifier: Modifier = Modifier,
    onRequestDefaultDialer: () -> Unit,
    onRequestPermissions: () -> Unit,
    isDefaultDialer: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDefaultDialer) Color.Green.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isDefaultDialer) "✅ Default Dialer Active" else "❌ Not Default Dialer",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDefaultDialer) Color.Green else Color.Red
                )
                Text(
                    text = if (isDefaultDialer)
                        "You can make and receive calls with this app"
                    else
                        "Set as default dialer to enable calling features",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Button(
            onClick = onRequestDefaultDialer,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isDefaultDialer
        ) {
            Text(if (isDefaultDialer) "Already Default Dialer" else "Set as Default Dialer")
        }

        Button(
            onClick = onRequestPermissions,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Request Call Permissions")
        }
    }
}

// New InCall Screen component for the main app
@Composable
fun InCallScreen(
    callState: CallState,
    onEndCall: () -> Unit,
    onAnswerCall: () -> Unit,
    onRejectCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Use the existing InCallScreen from screens package
    voxity.org.dialer.screens.InCallScreen(
        callState = callState,
        modifier = modifier
    )
}