package voxity.org.dialer.presentation.components

import androidx.compose.runtime.Composable

expect @Composable fun DialerPopup(
    onDismiss: () -> Unit,
    onCall: (String) -> Unit
)