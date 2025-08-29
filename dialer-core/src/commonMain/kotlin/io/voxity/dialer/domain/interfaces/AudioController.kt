package io.voxity.dialer.domain.interfaces

import io.voxity.dialer.domain.models.CallResult
import kotlinx.coroutines.flow.StateFlow

interface AudioController {
    val isMuted: StateFlow<Boolean>

    suspend fun setMute(muted: Boolean): CallResult
    suspend fun toggleMute(): CallResult
    suspend fun increaseVolume(): CallResult
    suspend fun decreaseVolume(): CallResult
    suspend fun requestAudioFocus(): CallResult
    suspend fun abandonAudioFocus(): CallResult
    fun getCurrentMuteState(): Boolean
}