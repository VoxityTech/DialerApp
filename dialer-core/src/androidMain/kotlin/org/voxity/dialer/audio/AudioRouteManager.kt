package org.voxity.dialer.audio

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    fun refreshRoutesForCall() {
        updateAvailableRoutes()
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
        return try {
            when {
                audioManager.isSpeakerphoneOn -> AudioRoute.SPEAKER
                isBluetoothScoConnected() -> AudioRoute.BLUETOOTH
                isWiredHeadsetConnected() -> AudioRoute.WIRED_HEADSET
                else -> AudioRoute.EARPIECE
            }
        } catch (e: Exception) {
            AudioRoute.EARPIECE // Fallback to earpiece
        }
    }

    fun setAudioRoute(route: AudioRoute) {
        try {
            // Set communication mode FIRST
            if (audioManager.mode != AudioManager.MODE_IN_COMMUNICATION) {
                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            }

            when (route) {
                AudioRoute.SPEAKER -> {
                    // Clear other routes first
                    audioManager.isBluetoothScoOn = false
                    audioManager.stopBluetoothSco()

                    // Enable speaker with proper sequence
                    audioManager.isSpeakerphoneOn = true

                    // For Android 12+, also set communication device
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        audioManager.availableCommunicationDevices
                            .find { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                            ?.let { device ->
                                audioManager.setCommunicationDevice(device)
                            }
                    }
                }
                AudioRoute.EARPIECE -> {
                    audioManager.isSpeakerphoneOn = false
                    audioManager.isBluetoothScoOn = false
                    audioManager.stopBluetoothSco()
                    // Force route to earpiece - handle nullable case
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        audioManager.availableCommunicationDevices
                            .find { it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE }
                            ?.let { device ->
                                audioManager.setCommunicationDevice(device)
                            }
                    }
                }
                AudioRoute.BLUETOOTH -> {
                    audioManager.isSpeakerphoneOn = false
                    audioManager.isBluetoothScoOn = true
                    audioManager.startBluetoothSco()
                    // Handle Bluetooth device routing
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        audioManager.availableCommunicationDevices
                            .find { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO }
                            ?.let { device ->
                                audioManager.setCommunicationDevice(device)
                            }
                    }
                }
                AudioRoute.WIRED_HEADSET -> {
                    audioManager.isSpeakerphoneOn = false
                    audioManager.isBluetoothScoOn = false
                    audioManager.stopBluetoothSco()
                    // Handle wired headset routing
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        audioManager.availableCommunicationDevices
                            .find {
                                it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                                        it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                                        it.type == AudioDeviceInfo.TYPE_USB_HEADSET
                            }
                            ?.let { device ->
                                audioManager.setCommunicationDevice(device)
                            }
                    }
                }
            }

            _currentRoute.value = route

            Handler(Looper.getMainLooper()).postDelayed({
                val actualRoute = getCurrentRoute()
                if (_currentRoute.value != actualRoute) {
                    _currentRoute.value = actualRoute
                }
            }, 100)

        } catch (e: Exception) {
            Log.e("AudioRouteManager", "Failed to set audio route", e)
        }
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
        val detectedRoute = getCurrentRoute()
        if (_currentRoute.value != detectedRoute) {
            _currentRoute.value = detectedRoute
        }
    }

    private fun isBluetoothAvailable(): Boolean {
        return try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                return false
            }

            // Check if any Bluetooth device is connected for audio
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                @Suppress("MissingPermission")
                bluetoothHeadset?.connectedDevices?.isNotEmpty() == true
            } else {
                @Suppress("MissingPermission", "DEPRECATION")
                bluetoothHeadset?.connectedDevices?.isNotEmpty() == true
            }
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun isBluetoothScoConnected(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioManager.isBluetoothScoOn || isBluetoothAudioConnected()
            } else {
                audioManager.isBluetoothScoOn
            }
        } catch (e: Exception) {
            false
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