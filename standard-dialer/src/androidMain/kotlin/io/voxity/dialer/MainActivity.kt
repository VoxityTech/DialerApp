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
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import io.voxity.dialer.audio.CallAudioManager
import io.voxity.dialer.audio.VolumeKeyHandler
import io.voxity.dialer.domain.models.Contact
import io.voxity.dialer.domain.usecases.CallUseCases
import io.voxity.dialer.managers.CallManager
import io.voxity.dialer.sensors.ProximitySensorManager
import io.voxity.dialer.ui.callbacks.DialerNavigationCallbacks
import io.voxity.dialer.ui.state.DialerNavigationState
import io.voxity.dialer.ui.theme.DialerTheme
import kotlinx.coroutines.*
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {

    private lateinit var proximitySensorManager: ProximitySensorManager
    private lateinit var volumeKeyHandler: VolumeKeyHandler
    private lateinit var callAudioManager: CallAudioManager
    private lateinit var callManager: CallManager
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // UI State - using the new clean API
    private var navigationState by mutableStateOf(DialerNavigationState())
    private var contactsState by mutableStateOf(DialerState.contacts())
    private var callHistoryState by mutableStateOf(DialerState.callHistory())
    private var dialerState by mutableStateOf(DialerState.keypad())
    private var activeCallState by mutableStateOf(DialerState.activeCall())

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            loadData()
        } else {
            Toast.makeText(this, "Some permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val defaultDialerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "App set as default dialer", Toast.LENGTH_SHORT).show()
            navigationState = navigationState.copy(isDefaultDialer = true)
            dialerState = DialerState.keypad(phoneNumber = dialerState.phoneNumber, canMakeCall = true)
        } else {
            Toast.makeText(this, "Not set as default dialer", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupCallHandling()
        initializeDependencies()

        val isDefaultDialer = isDefaultDialerApp()
        navigationState = DialerNavigationState(isDefaultDialer = isDefaultDialer)
        dialerState = DialerState.keypad(canMakeCall = isDefaultDialer)

        handleDialIntent(intent)

        setContent {
            DialerTheme {
                // Use the legacy DialerUI for now, or create a new navigation structure
                DialerUI(
                    navigationState = navigationState,
                    contactsState = contactsState,
                    callHistoryState = callHistoryState,
                    activeCallState = activeCallState,
                    navigationCallbacks = createNavigationCallbacks(),
                    contactsCallbacks = createContactsCallbacks(),
                    callHistoryCallbacks = createCallHistoryCallbacks(),
                    activeCallCallbacks = createActiveCallCallbacks(),
                    dialerCallbacks = createDialerCallbacks()
                )
            }
        }

        if (!hasAllPermissions()) {
            requestAllPermissions()
        } else {
            loadData()
        }

        if (!isDefaultDialerApp()) {
            showDefaultDialerPrompt()
        }
    }

    private fun setupCallHandling() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        volumeControlStream = AudioManager.STREAM_RING
    }

    private fun initializeDependencies() {
        proximitySensorManager = get()
        callAudioManager = get()
        callManager = get<io.voxity.dialer.domain.repository.CallRepository>() as CallManager

        volumeKeyHandler = VolumeKeyHandler(
            onVolumeUp = {
                scope.launch { callAudioManager.increaseVolume() }
            },
            onVolumeDown = {
                scope.launch { callAudioManager.decreaseVolume() }
            },
            onSilenceRingtone = {
                callManager.ringtoneManager.silenceRinging()
            }
        )

        // Observe call state
        scope.launch {
            callManager.currentCallState.collect { callState ->
                activeCallState = DialerState.activeCall(
                    callState = callState,
                    callDuration = activeCallState.callDuration,
                    showAudioSelector = activeCallState.showAudioSelector
                )
            }
        }
    }

    private fun loadData() {
        scope.launch {
            contactsState = DialerState.contacts(isLoading = true)
            callHistoryState = DialerState.callHistory(isLoading = true)

            try {
                val callUseCases = get<CallUseCases>()

                // Load contacts
                val contacts = callUseCases.getContacts()
                contactsState = DialerState.contacts(
                    contacts = contacts,
                    filteredContacts = contacts,
                    isLoading = false
                )

                // Load call history
                val callHistory = callUseCases.getCallHistory()
                callHistoryState = DialerState.callHistory(
                    callHistory = callHistory,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading data", e)
                contactsState = DialerState.contacts(isLoading = false)
                callHistoryState = DialerState.callHistory(isLoading = false)
            }
        }
    }

    private fun createNavigationCallbacks() = object : DialerNavigationCallbacks {
        override fun onTabSelected(screenId: Any) {
            navigationState = navigationState.copy(selectedTab = screenId)
        }

        override fun onShowDialerModal() {
            navigationState = navigationState.copy(showDialerModal = true)
        }

        override fun onHideDialerModal() {
            navigationState = navigationState.copy(showDialerModal = false)
            dialerState = DialerState.keypad(canMakeCall = dialerState.canMakeCall)
        }

        override fun onRequestDefaultDialer() {
            requestDefaultDialerRole()
        }

        override fun onRequestPermissions() {
            requestAllPermissions()
        }
    }

    private fun createContactsCallbacks() = DialerCallbacks.contacts(
        onContactSelected = { contact ->
            // Handle contact selection
        },
        onCallContact = { phoneNumber ->
            makeCall(phoneNumber)
        },
        onSearchQueryChanged = { query ->
            val filtered = if (query.isBlank()) {
                contactsState.contacts
            } else {
                contactsState.contacts.filter { contact ->
                    contact.name.contains(query, ignoreCase = true) ||
                            contact.phoneNumbers.any { it.contains(query) }
                }
            }

            contactsState = DialerState.contacts(
                contacts = contactsState.contacts,
                isLoading = contactsState.isLoading,
                searchQuery = query,
                filteredContacts = filtered
            )
        }
    )

    private fun createCallHistoryCallbacks() = DialerCallbacks.callHistory(
        onCallHistoryItemClicked = { phoneNumber ->
            makeCall(phoneNumber)
        },
        onRefresh = {
            loadData()
        }
    )

    private fun createActiveCallCallbacks() = DialerCallbacks.activeCall(
        onAnswerCall = {
            scope.launch {
                get<CallUseCases>().answerCall()
            }
        },
        onRejectCall = {
            scope.launch {
                get<CallUseCases>().rejectCall()
            }
        },
        onEndCall = {
            scope.launch {
                get<CallUseCases>().endCall()
            }
        },
        onHoldCall = {
            scope.launch {
                get<CallUseCases>().holdCall()
            }
        },
        onUnholdCall = {
            scope.launch {
                get<CallUseCases>().unholdCall()
            }
        },
        onMuteCall = { muted ->
            scope.launch {
                get<CallUseCases>().muteCall(muted)
            }
        },
        onShowAudioSelector = {
            activeCallState = DialerState.activeCall(
                callState = activeCallState.callState,
                callDuration = activeCallState.callDuration,
                showAudioSelector = true
            )
        },
        onHideAudioSelector = {
            activeCallState = DialerState.activeCall(
                callState = activeCallState.callState,
                callDuration = activeCallState.callDuration,
                showAudioSelector = false
            )
        }
    )

    private fun createDialerCallbacks() = DialerCallbacks.keypad(
        onNumberChanged = { number ->
            dialerState = DialerState.keypad(
                phoneNumber = dialerState.phoneNumber + number,
                canMakeCall = dialerState.canMakeCall
            )
        },
        onMakeCall = { phoneNumber ->
            makeCall(phoneNumber)
        },
        onDeleteDigit = {
            val currentNumber = dialerState.phoneNumber
            if (currentNumber.isNotEmpty()) {
                dialerState = DialerState.keypad(
                    phoneNumber = currentNumber.dropLast(1),
                    canMakeCall = dialerState.canMakeCall
                )
            }
        },
        onClearNumber = {
            dialerState = DialerState.keypad(canMakeCall = dialerState.canMakeCall)
        }
    )

    private fun makeCall(phoneNumber: String) {
        if (!dialerState.canMakeCall) {
            Toast.makeText(this, "Set as default dialer to make calls", Toast.LENGTH_SHORT).show()
            requestDefaultDialerRole()
            return
        }

        scope.launch {
            try {
                get<CallUseCases>().makeCall(phoneNumber)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error making call", e)
                Toast.makeText(this@MainActivity, "Failed to make call", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            val callState = activeCallState.callState
            val isRinging = callState.isRinging && callState.isIncoming
            val isInCall = callState.isActive || callState.isOnHold

            if (isRinging) {
                callManager.ringtoneManager.silenceRinging()
                return true
            } else if (isInCall) {
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
                val callState = activeCallState.callState
                val isRinging = callState.isRinging && callState.isIncoming

                if (isRinging) {
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

        val callState = activeCallState.callState
        val isInCall = callState.isActive || callState.isRinging
        proximitySensorManager.setCallActive(isInCall)
    }

    override fun onPause() {
        super.onPause()
        proximitySensorManager.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
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

        when (intent.action) {
            Intent.ACTION_DIAL -> {
                val number = intent.data?.schemeSpecificPart
                number?.let {
                    dialerState = DialerState.keypad(phoneNumber = it, canMakeCall = dialerState.canMakeCall)
                    navigationState = navigationState.copy(showDialerModal = true)
                }
            }
            Intent.ACTION_CALL -> {
                val number = intent.data?.schemeSpecificPart
                if (!number.isNullOrEmpty() && isDefaultDialerApp()) {
                    makeCall(number)
                } else if (!isDefaultDialerApp()) {
                    Toast.makeText(this, "Set as default dialer to handle call intents.", Toast.LENGTH_LONG).show()
                    requestDefaultDialerRole()
                }
            }
            "android.intent.action.CALL_PRIVILEGED" -> {
                val number = intent.data?.schemeSpecificPart
                if (!number.isNullOrEmpty() && isDefaultDialerApp()) {
                    makeCall(number)
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
                } else if (!roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
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