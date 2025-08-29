package io.voxity.dialer.audio

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AudioRoute(val displayName: String) {
    SPEAKER("Speaker"),
    EARPIECE("Earpiece"),
    BLUETOOTH("Bluetooth"),
    WIRED_HEADSET("Wired Headset")
}

class AudioRouteManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var bluetoothHeadset: BluetoothHeadset? = null

    private val _currentRoute = MutableStateFlow(AudioRoute.EARPIECE)
    val currentRoute: StateFlow<AudioRoute> = _currentRoute.asStateFlow()

    private val _availableRoutes = MutableStateFlow<List<AudioRoute>>(listOf(AudioRoute.EARPIECE))
    val availableRoutes: StateFlow<List<AudioRoute>> = _availableRoutes.asStateFlow()

    init {
        updateAvailableRoutes()
        initBluetoothProfile()
    }

    private fun initBluetoothProfile() {
        try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            bluetoothAdapter?.getProfileProxy(
                context,
                object : BluetoothProfile.ServiceListener {
                    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                        if (profile == BluetoothProfile.HEADSET) {
                            bluetoothHeadset = proxy as BluetoothHeadset
                            updateAvailableRoutes()
                        }
                    }

                    override fun onServiceDisconnected(profile: Int) {
                        if (profile == BluetoothProfile.HEADSET) {
                            bluetoothHeadset = null
                            updateAvailableRoutes()
                        }
                    }
                },
                BluetoothProfile.HEADSET
            )
        } catch (e: Exception) {
            // Bluetooth not available
        }
    }

    fun getAvailableRoutes(): List<AudioRoute> {
        return _availableRoutes.value
    }

    fun getCurrentRoute(): AudioRoute {
        return when {
            audioManager.isSpeakerphoneOn -> AudioRoute.SPEAKER
            isBluetoothScoConnected() -> AudioRoute.BLUETOOTH
            isWiredHeadsetConnected() -> AudioRoute.WIRED_HEADSET
            else -> AudioRoute.EARPIECE
        }
    }

    fun setAudioRoute(route: AudioRoute) {
        when (route) {
            AudioRoute.SPEAKER -> {
                audioManager.isSpeakerphoneOn = true
                audioManager.isBluetoothScoOn = false
                audioManager.stopBluetoothSco()
            }
            AudioRoute.EARPIECE -> {
                audioManager.isSpeakerphoneOn = false
                audioManager.isBluetoothScoOn = false
                audioManager.stopBluetoothSco()
            }
            AudioRoute.BLUETOOTH -> {
                audioManager.isSpeakerphoneOn = false
                audioManager.isBluetoothScoOn = true
                audioManager.startBluetoothSco()
            }
            AudioRoute.WIRED_HEADSET -> {
                audioManager.isSpeakerphoneOn = false
                audioManager.isBluetoothScoOn = false
                audioManager.stopBluetoothSco()
            }
        }
        _currentRoute.value = route
    }

    private fun updateAvailableRoutes() {
        val routes = mutableListOf<AudioRoute>()

        routes.add(AudioRoute.EARPIECE)
        routes.add(AudioRoute.SPEAKER)

        if (isBluetoothAvailable()) {
            routes.add(AudioRoute.BLUETOOTH)
        }

        if (isWiredHeadsetConnected()) {
            routes.add(AudioRoute.WIRED_HEADSET)
        }

        _availableRoutes.value = routes
        _currentRoute.value = getCurrentRoute()
    }

    private fun isBluetoothAvailable(): Boolean {
        return try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                return false
            }

            // For Android 12+ we need to check permissions differently
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                bluetoothHeadset?.connectedDevices?.isNotEmpty() == true
            } else {
                @Suppress("MissingPermission")
                bluetoothHeadset?.connectedDevices?.isNotEmpty() == true
            }
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun isBluetoothScoConnected(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager.isBluetoothScoOn || isBluetoothAudioConnected()
        } else {
            audioManager.isBluetoothScoOn
        }
    }

    private fun isBluetoothAudioConnected(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any { device ->
                device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                        device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
            }
        } else {
            false
        }
    }

    private fun isWiredHeadsetConnected(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any { device ->
                device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                        device.type == AudioDeviceInfo.TYPE_USB_HEADSET
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.isWiredHeadsetOn
        }
    }
}