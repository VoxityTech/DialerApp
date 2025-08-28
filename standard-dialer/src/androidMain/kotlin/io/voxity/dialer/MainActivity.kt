package io.voxity.dialer

import android.Manifest
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import io.voxity.dialer.audio.CallAudioManager
import io.voxity.dialer.audio.VolumeKeyHandler
import io.voxity.dialer.managers.CallManager
import io.voxity.dialer.sensors.ProximitySensorManager
import io.voxity.dialer.ui.viewmodel.DialerViewModel
import org.koin.android.ext.android.get
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {

    private lateinit var proximitySensorManager: ProximitySensorManager
    private lateinit var volumeKeyHandler: VolumeKeyHandler
    private lateinit var callAudioManager: CallAudioManager
    private lateinit var callManager: CallManager
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Some permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val defaultDialerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "App set as default dialer", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Not set as default dialer", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable showing over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        proximitySensorManager = ProximitySensorManager(this)
        callAudioManager = CallAudioManager(this)
        callManager = CallManager.Companion.getInstance(this)

        volumeKeyHandler = VolumeKeyHandler(
            onVolumeUp = {
                callAudioManager.increaseCallVolume()
            },
            onVolumeDown = {
                callAudioManager.decreaseCallVolume()
            },
            onSilenceRingtone = {
                callManager.ringtoneManager.silenceRinging()
            }
        )

        volumeControlStream = AudioManager.STREAM_RING

        handleDialIntent(intent)

        setContent {
            val dialerViewModel = koinInject<DialerViewModel>()

            VoxityDialerApp(
                viewModel = dialerViewModel,
                onRequestDefaultDialer = { requestDefaultDialerRole() },
                onRequestPermissions = { requestAllPermissions() },
                isDefaultDialer = isDefaultDialerApp()
            )
        }

        if (!hasAllPermissions()) {
            requestAllPermissions()
        }

        if (!isDefaultDialerApp()) {
            showDefaultDialerPrompt()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Handle volume keys immediately, before checking call states
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            val callState = callManager.currentCallState.value
            val isRinging = callState.isRinging && callState.isIncoming
            val isInCall = callState.isActive || callState.isOnHold

            Log.d("VolumeKeys", "Volume key pressed - keyCode: $keyCode")
            Log.d("VolumeKeys", "Call state - isRinging: $isRinging, isActive: ${callState.isActive}, isIncoming: ${callState.isIncoming}")

            if (isRinging) {
                Log.d("VolumeKeys", "Silencing ringtone immediately")
                callManager.ringtoneManager.silenceRinging()
                return true // Consume the event
            } else if (isInCall) {
                Log.d("VolumeKeys", "Handling in-call volume")
                return volumeKeyHandler.handleKeyEvent(keyCode, event, false, true)
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val keyCode = event.keyCode
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                val callState = callManager.currentCallState.value
                val isRinging = callState.isRinging && callState.isIncoming

                Log.d("DispatchKey", "Dispatching volume key - keyCode: $keyCode, isRinging: $isRinging")

                if (isRinging) {
                    Log.d("DispatchKey", "Intercepting and silencing")
                    callManager.ringtoneManager.silenceRinging()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onResume() {
        super.onResume()
        proximitySensorManager.startListening()

        val activeCallManager = this.callManager
        val callState = activeCallManager.currentCallState.value

        if (callState != null) {
            val isInCall = callState.isActive || callState.isRinging
            proximitySensorManager.setCallActive(isInCall)
        } else {
            // Log an error or handle the case where callState is null
            Log.e("MainActivity", "CallState is null in onResume")
            // Optionally set a default for proximity sensor if state is unknown
            proximitySensorManager.setCallActive(false)
        }
    }

    override fun onPause() {
        super.onPause()
        proximitySensorManager.stopListening()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDialIntent(intent)
    }

    private fun handleDialIntent(intent: Intent?) {
        intent ?: return

        if (intent.hasExtra("FROM_DIALER_APP")) {
            return
        }

        val dialerViewModel = get<DialerViewModel>()

        when (intent.action) {
            Intent.ACTION_DIAL -> {
                val number = intent.data?.schemeSpecificPart
                number?.let {
                    dialerViewModel.onDialIntentReceived(it)
                }
            }
            Intent.ACTION_CALL -> {
                val number = intent.data?.schemeSpecificPart
                if (!number.isNullOrEmpty() && isDefaultDialerApp()) {
                    dialerViewModel.onCallIntentReceived(number)
                } else if (!isDefaultDialerApp()) {
                    Toast.makeText(this, "Set as default dialer to handle call intents.", Toast.LENGTH_LONG).show()
                    requestDefaultDialerRole()
                }
            }
            "android.intent.action.CALL_PRIVILEGED" -> {
                val number = intent.data?.schemeSpecificPart
                if (!number.isNullOrEmpty() && isDefaultDialerApp()) {
                    dialerViewModel.onCallIntentReceived(number)
                } else if (!isDefaultDialerApp()) {
                    Toast.makeText(this, "Set as default dialer to handle privileged call intents.", Toast.LENGTH_LONG).show()
                    requestDefaultDialerRole()
                }
            }
        }
    }

    private fun hasAllPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.MANAGE_OWN_CALLS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ANSWER_PHONE_CALLS
        )
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestAllPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.MANAGE_OWN_CALLS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ANSWER_PHONE_CALLS
        )
        permissionLauncher.launch(permissions)
    }

    private fun isDefaultDialerApp(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(ROLE_SERVICE) as RoleManager
            roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
        } else {
            val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
            packageName == telecomManager.defaultDialerPackage
        }
    }

    private fun showDefaultDialerPrompt() {
        Toast.makeText(this, "Please set this app as default dialer in settings", Toast.LENGTH_LONG).show()
    }

    fun requestDefaultDialerRole() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(ROLE_SERVICE) as RoleManager
                if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
                    !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                    defaultDialerLauncher.launch(intent)
                } else if (!roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)){
                    Toast.makeText(this, "Dialer role not available on this device.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "App is already default or role cannot be requested.", Toast.LENGTH_SHORT).show()
                }
            } else {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                    putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                }
                if (intent.resolveActivity(packageManager) != null) {
                    defaultDialerLauncher.launch(intent)
                } else {
                    Toast.makeText(this, "Cannot change default dialer on this device version.", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error requesting default dialer role: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}