package voxity.org.dialer.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import voxity.org.dialer.domain.usecases.CallUseCases

expect @Composable fun CallHistoryScreen(
    modifier: Modifier = Modifier,
    callUseCases: CallUseCases
)