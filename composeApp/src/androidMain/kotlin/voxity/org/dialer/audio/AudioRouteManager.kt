package voxity.org.dialer.audio

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import voxity.org.dialer.presentation.components.AudioRoute

class AudioRouteManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun getAvailableRoutes(): List<AudioRoute> {
        val routes = mutableListOf<AudioRoute>()

        // Always available
        routes.add(AudioRoute.EARPIECE)
        routes.add(AudioRoute.SPEAKER)

        // Check for Bluetooth
        if (isBluetoothAvailable()) {
            routes.add(AudioRoute.BLUETOOTH)
        }

        // Check for wired headset
        if (isWiredHeadsetConnected()) {
            routes.add(AudioRoute.WIRED_HEADSET)
        }

        return routes
    }

    fun getCurrentRoute(): AudioRoute {
        return when {
            audioManager.isSpeakerphoneOn -> AudioRoute.SPEAKER
            isBluetoothConnected() -> AudioRoute.BLUETOOTH
            isWiredHeadsetConnected() -> AudioRoute.WIRED_HEADSET
            else -> AudioRoute.EARPIECE
        }
    }

    fun setAudioRoute(route: AudioRoute) {
        when (route) {
            AudioRoute.SPEAKER -> {
                audioManager.isSpeakerphoneOn = true
                audioManager.isBluetoothScoOn = false
            }
            AudioRoute.EARPIECE -> {
                audioManager.isSpeakerphoneOn = false
                audioManager.isBluetoothScoOn = false
            }
            AudioRoute.BLUETOOTH -> {
                audioManager.isSpeakerphoneOn = false
                audioManager.isBluetoothScoOn = true
                audioManager.startBluetoothSco()
            }
            AudioRoute.WIRED_HEADSET -> {
                audioManager.isSpeakerphoneOn = false
                audioManager.isBluetoothScoOn = false
            }
        }
    }

    private fun isBluetoothAvailable(): Boolean {
        return try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            bluetoothAdapter != null && bluetoothAdapter.isEnabled
        } catch (e: Exception) {
            false
        }
    }

    private fun isBluetoothConnected(): Boolean {
        return audioManager.isBluetoothScoOn || audioManager.isBluetoothA2dpOn
    }

    private fun isWiredHeadsetConnected(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any { device ->
                device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.isWiredHeadsetOn
        }
    }
}