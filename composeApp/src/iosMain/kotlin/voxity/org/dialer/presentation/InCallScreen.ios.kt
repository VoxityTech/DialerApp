package voxity.org.dialer.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import voxity.org.dialer.domain.models.CallState
import voxity.org.dialer.domain.usecases.CallUseCases

@Composable
actual fun InCallScreen(
    callState: CallState,
    callUseCases: CallUseCases,
    modifier: Modifier
) {
}