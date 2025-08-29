package io.voxity.dialer.audio

import android.content.Context
import android.media.AudioManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.os.Build
import io.voxity.dialer.domain.interfaces.AudioController
import io.voxity.dialer.domain.models.CallResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CallAudioManager(private val context: Context) : AudioController {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _isMuted = MutableStateFlow(false)
    override val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private var audioFocusRequest: AudioFocusRequest? = null
    private var previousAudioMode = AudioManager.MODE_NORMAL

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Resume normal audio processing
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Can't call suspend function from listener, do it synchronously
                abandonAudioFocusSync()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Temporarily lost audio focus
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lost audio focus for a short time, can duck
            }
        }
    }

    // Add this non-suspend version for use in listeners
    private fun abandonAudioFocusSync() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(audioFocusChangeListener)
            }
            audioManager.mode = previousAudioMode
        } catch (e: Exception) {
            // Log error but don't throw since this is called from listener
            android.util.Log.e("CallAudioManager", "Failed to abandon audio focus", e)
        }
    }

    // Modify the setMute method to ensure proper state sync
    override suspend fun setMute(muted: Boolean): CallResult = withContext(Dispatchers.Main) {
        try {
            // Set both the system mute state and our internal state
            audioManager.isMicrophoneMute = muted
            _isMuted.value = muted

            // Verify the state was set correctly
            val actualState = audioManager.isMicrophoneMute
            if (actualState != muted) {
                _isMuted.value = actualState // Sync with actual state
            }

            CallResult.Success
        } catch (e: Exception) {
            // If setting failed, sync our state with system state
            _isMuted.value = audioManager.isMicrophoneMute
            CallResult.Error("Failed to set mute state", e)
        }
    }

    // Also update syncMuteState to be more robust
    fun syncMuteState() {
        try {
            _isMuted.value = audioManager.isMicrophoneMute
        } catch (e: Exception) {
            android.util.Log.e("CallAudioManager", "Failed to sync mute state", e)
        }
    }

    override suspend fun toggleMute(): CallResult {
        val newMuteState = !_isMuted.value
        return setMute(newMuteState)
    }

    override fun getCurrentMuteState(): Boolean {
        return try {
            audioManager.isMicrophoneMute
        } catch (e: Exception) {
            _isMuted.value
        }
    }

    override suspend fun increaseVolume(): CallResult = withContext(Dispatchers.Main) {
        try {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI
            )
            CallResult.Success
        } catch (e: Exception) {
            CallResult.Error("Failed to increase volume", e)
        }
    }

    override suspend fun decreaseVolume(): CallResult = withContext(Dispatchers.Main) {
        try {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI
            )
            CallResult.Success
        } catch (e: Exception) {
            CallResult.Error("Failed to decrease volume", e)
        }
    }

    override suspend fun requestAudioFocus(): CallResult = withContext(Dispatchers.Main) {
        try {
            previousAudioMode = audioManager.mode
            audioManager.mode = AudioManager.MODE_IN_CALL

            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()

                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .setAcceptsDelayedFocusGain(false)
                    .build()

                audioManager.requestAudioFocus(audioFocusRequest!!)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                CallResult.Success
            } else {
                CallResult.Error("Audio focus request denied")
            }
        } catch (e: Exception) {
            CallResult.Error("Failed to request audio focus", e)
        }
    }

    override suspend fun abandonAudioFocus(): CallResult = withContext(Dispatchers.Main) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(audioFocusChangeListener)
            }

            audioManager.mode = previousAudioMode
            CallResult.Success
        } catch (e: Exception) {
            CallResult.Error("Failed to abandon audio focus", e)
        }
    }
}