package org.voxity.dialer.audio

import android.view.KeyEvent
import android.util.Log

class VolumeKeyHandler(
    private val onVolumeUp: () -> Unit,
    private val onVolumeDown: () -> Unit,
    private val onSilenceRingtone: () -> Unit
) {

    private val TAG = "VolumeKeyHandler"

    fun handleKeyEvent(keyCode: Int, event: KeyEvent?, isRinging: Boolean = false, isInCall: Boolean = false): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    Log.d(TAG, "Volume UP pressed - isRinging: $isRinging, isInCall: $isInCall")
                    if (isRinging) {
                        Log.d(TAG, "Silencing ringtone")
                        onSilenceRingtone()
                    } else if (isInCall) {
                        Log.d(TAG, "Increasing call volume")
                        onVolumeUp()
                    }
                    return true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    Log.d(TAG, "Volume DOWN pressed - isRinging: $isRinging, isInCall: $isInCall")
                    if (isRinging) {
                        Log.d(TAG, "Silencing ringtone")
                        onSilenceRingtone()
                    } else if (isInCall) {
                        Log.d(TAG, "Decreasing call volume")
                        onVolumeDown()
                    }
                    return true
                }
                KeyEvent.KEYCODE_VOLUME_MUTE -> {
                    Log.d(TAG, "Volume MUTE pressed")
                    if (isRinging) {
                        onSilenceRingtone()
                    }
                    return true
                }
            }
        }
        return false
    }
}