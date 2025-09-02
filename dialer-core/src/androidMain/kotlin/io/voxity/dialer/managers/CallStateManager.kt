package io.voxity.dialer.managers

import kotlinx.coroutines.flow.*

class CallStateManager {
    private val _uiState = MutableStateFlow(CallUIState())
    val uiState: StateFlow<CallUIState> = _uiState.asStateFlow()

    fun updateCallVisibility(isVisible: Boolean) {
        _uiState.update { it.copy(isCallVisible = isVisible) }
    }

    data class CallUIState(
        val isCallVisible: Boolean = false,
        val shouldRefreshHistory: Boolean = false
    )
}