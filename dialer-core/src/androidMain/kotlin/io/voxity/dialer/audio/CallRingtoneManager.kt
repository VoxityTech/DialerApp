package io.voxity.dialer.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import android.util.Log

class CallRingtoneManager(private val context: Context) {

    private val TAG = "CallRingtoneManager"
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var mediaPlayer: MediaPlayer? = null
    private var systemRingtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var isRinging = false
    private var isSilenced = false

    private var originalRingtoneVolume = -1

    init {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun startRinging() {
        Log.d(TAG, "startRinging() called")
        if (isRinging) {
            Log.d(TAG, "Already ringing, ignoring")
            return
        }

        originalRingtoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)

        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            startRinging(ringtoneUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting ringtone", e)
            startDefaultRinging()
        }
    }

    private fun startRinging(ringtoneUri: Uri) {
        try {
            isRinging = true
            isSilenced = false
            Log.d(TAG, "Starting ringtone playback")

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, ringtoneUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }

            startVibration()

        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer failed, trying system ringtone", e)
            startSystemRingtone()
        }
    }

    private fun startSystemRingtone() {
        try {
            isRinging = true
            isSilenced = false
            Log.d(TAG, "Using system ringtone")

            systemRingtone = RingtoneManager.getRingtone(
                context,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            )
            systemRingtone?.play()
            startVibration()
        } catch (e: Exception) {
            Log.e(TAG, "System ringtone failed", e)
            startDefaultRinging()
        }
    }

    private fun startDefaultRinging() {
        try {
            isRinging = true
            Log.d(TAG, "Fallback to vibration only")
            startVibration()
        } catch (e: Exception) {
            Log.e(TAG, "All ringtone methods failed", e)
        }
    }

    private fun startVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                val vibrationEffect = VibrationEffect.createWaveform(pattern, 0)
                vibrator?.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting vibration", e)
        }
    }

    fun silenceRinging() {
        Log.d(TAG, "silenceRinging() called - isRinging: $isRinging, isSilenced: $isSilenced")
        if (!isRinging) {
            Log.d(TAG, "Not ringing, ignoring")
            return
        }

        if (isSilenced) {
            Log.d(TAG, "Already silenced, ignoring")
            return
        }

        try {
            isSilenced = true
            Log.d(TAG, "Silencing ringtone with multiple methods")

            // Method 1: AudioManager stream muting
            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)

            // Method 2: MediaPlayer volume
            mediaPlayer?.setVolume(0f, 0f)

            // Method 3: System ringtone stop
            systemRingtone?.stop()

            // Method 4: Stop vibration
            vibrator?.cancel()

            Log.d(TAG, "All silencing methods applied")

        } catch (e: Exception) {
            Log.e(TAG, "Error in silenceRinging", e)
            stopRinging()
        }
    }

    fun stopRinging() {
        Log.d(TAG, "stopRinging() called")
        try {
            isRinging = false
            isSilenced = false

            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            systemRingtone?.stop()
            systemRingtone = null

            vibrator?.cancel()

            if (originalRingtoneVolume >= 0) {
                audioManager.setStreamVolume(AudioManager.STREAM_RING, originalRingtoneVolume, 0)
                originalRingtoneVolume = -1
            }

            Log.d(TAG, "Ringtone stopped successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping ringtone", e)
        }
    }

    fun isCurrentlyRinging(): Boolean = isRinging && !isSilenced
    fun isSilent(): Boolean = isSilenced
}