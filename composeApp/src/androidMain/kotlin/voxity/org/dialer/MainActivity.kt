package voxity.org.dialer

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import org.koin.compose.koinInject
import voxity.org.dialer.domain.usecases.CallUseCases

class MainActivity : ComponentActivity() {

    // Remove this line - it's causing the crash
    // private val callManager = CallManager.getInstance()

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
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "App set as default dialer", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Not set as default dialer", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the application context for platform functions
        initializeContext(this)

        // Handle incoming DIAL intents
        handleDialIntent(intent)

        setContent {
            val callUseCases = koinInject<CallUseCases>()
            var showInCall by remember { mutableStateOf(false) }

            // Observe call state
            val callState by callUseCases.callState.collectAsState()

            LaunchedEffect(callState.isActive, callState.isRinging, callState.isConnecting) {
                showInCall = callState.isActive || callState.isRinging || callState.isConnecting
            }

            if (showInCall) {
                // Show in-call UI - pass callUseCases to the screen
                InCallScreen(
                    callState = callState,
                    callUseCases = callUseCases
                )
            } else {
                // Show main app
                App(
                    onRequestDefaultDialer = { requestDefaultDialerRole() },
                    onRequestPermissions = { requestAllPermissions() },
                    isDefaultDialer = isDefaultDialerApp(),
                    callUseCases = callUseCases
                )
            }
        }

        // Check and request permissions
        if (!hasAllPermissions()) {
            requestAllPermissions()
        }

        // Check if we're the default dialer
        if (!isDefaultDialerApp()) {
            showDefaultDialerPrompt()
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDialIntent(intent)
    }

    private fun handleDialIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_DIAL -> {
                val number = intent.data?.schemeSpecificPart
                number?.let {
                    Toast.makeText(this, "Dialing: $it", Toast.LENGTH_SHORT).show()
                }
            }
            Intent.ACTION_CALL -> {
                val number = intent.data?.schemeSpecificPart
                if (!number.isNullOrEmpty() && isDefaultDialerApp()) {
                    // Use CallUseCases through Koin injection instead
                    makeCall(number)
                }
            }
            "android.intent.action.CALL_PRIVILEGED" -> {
                val number = intent.data?.schemeSpecificPart
                if (!number.isNullOrEmpty() && isDefaultDialerApp()) {
                    makeCall(number)
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
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
        } else {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            packageName == telecomManager.defaultDialerPackage
        }
    }

    private fun showDefaultDialerPrompt() {
        // Show a dialog or UI to prompt user to set as default
        Toast.makeText(this, "Please set this app as default dialer in settings", Toast.LENGTH_LONG).show()
    }

    fun requestDefaultDialerRole() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
                if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
                    !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                    defaultDialerLauncher.launch(intent)
                }
            } else {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                    putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                }
                defaultDialerLauncher.launch(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error requesting default dialer role", Toast.LENGTH_SHORT).show()
        }
    }
}