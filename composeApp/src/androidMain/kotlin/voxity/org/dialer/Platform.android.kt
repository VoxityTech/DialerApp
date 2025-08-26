package voxity.org.dialer

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import org.json.JSONObject

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

private lateinit var applicationContext: Context
private val PLATFORM_TAG = "PlatformCalls"

fun initializeContext(context: Context) {
    applicationContext = context.applicationContext
    Log.d(PLATFORM_TAG, "============ PLATFORM CONTEXT INITIALIZED ============")
    Log.d(PLATFORM_TAG, "Package name: ${context.packageName}")
    Log.d(PLATFORM_TAG, "Android version: ${Build.VERSION.SDK_INT}")
    Log.d(PLATFORM_TAG, "====================================================")
}

actual fun makeCall(phoneNumber: String) {
    Log.d(PLATFORM_TAG, "============ MAKE CALL REQUEST ============")
    Log.d(PLATFORM_TAG, "Phone Number: $phoneNumber")
    Log.d(PLATFORM_TAG, "Timestamp: ${System.currentTimeMillis()}")

    try {
        val telecomManager = applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        // Check if we have permission and are default dialer
        val hasCallPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        Log.d(PLATFORM_TAG, "Has CALL_PHONE permission: $hasCallPermission")

        if (hasCallPermission) {
            val uri = Uri.fromParts("tel", phoneNumber, null)
            val extras = Bundle().apply {
                putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false)
                putLong("CALL_REQUEST_TIMESTAMP", System.currentTimeMillis())
            }

            Log.d(PLATFORM_TAG, "Using TelecomManager.placeCall()")
            Log.d(PLATFORM_TAG, "URI: $uri")
            Log.d(PLATFORM_TAG, "Extras: ${extras.keySet().joinToString()}")

            // Log call attempt metadata
            logCallAttempt(phoneNumber, "TELECOM_MANAGER", extras)

            telecomManager.placeCall(uri, extras)
            Log.d(PLATFORM_TAG, "placeCall() method executed successfully")

        } else {
            Log.d(PLATFORM_TAG, "Using fallback ACTION_CALL intent")

            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            logCallAttempt(phoneNumber, "ACTION_CALL_INTENT", null)
            applicationContext.startActivity(callIntent)
            Log.d(PLATFORM_TAG, "ACTION_CALL intent sent")
        }

    } catch (e: Exception) {
        Log.e(PLATFORM_TAG, "============ MAKE CALL ERROR ============")
        Log.e(PLATFORM_TAG, "Error making call: ${e.message}")
        Log.e(PLATFORM_TAG, "Exception type: ${e.javaClass.simpleName}")
        Log.e(PLATFORM_TAG, "Stack trace: ${e.stackTrace.contentToString()}")
        Log.e(PLATFORM_TAG, "========================================")

        Toast.makeText(applicationContext, "Error making call: ${e.message}", Toast.LENGTH_SHORT).show()
    }

    Log.d(PLATFORM_TAG, "==========================================")
}

actual fun endCall() {
    Log.d(PLATFORM_TAG, "============ END CALL REQUEST ============")
    Log.d(PLATFORM_TAG, "Timestamp: ${System.currentTimeMillis()}")

    try {
        val telecomManager = applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val hasPermission = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ANSWER_PHONE_CALLS
            ) == PackageManager.PERMISSION_GRANTED

            Log.d(PLATFORM_TAG, "Has ANSWER_PHONE_CALLS permission: $hasPermission")
            Log.d(PLATFORM_TAG, "Android version supports endCall(): ${Build.VERSION.SDK_INT >= Build.VERSION_CODES.P}")

            if (hasPermission) {
                telecomManager.endCall()
                Log.d(PLATFORM_TAG, "TelecomManager.endCall() executed")
            } else {
                Log.w(PLATFORM_TAG, "Missing ANSWER_PHONE_CALLS permission")
            }
        } else {
            Log.w(PLATFORM_TAG, "endCall() not supported on Android < P (${Build.VERSION.SDK_INT})")
        }

        // Also notify our call manager
        voxity.org.dialer.managers.CallManager.getInstance().endCall()
        Log.d(PLATFORM_TAG, "CallManager.endCall() notified")

    } catch (e: Exception) {
        Log.e(PLATFORM_TAG, "============ END CALL ERROR ============")
        Log.e(PLATFORM_TAG, "Error ending call: ${e.message}")
        Log.e(PLATFORM_TAG, "Exception type: ${e.javaClass.simpleName}")
        Log.e(PLATFORM_TAG, "=======================================")
    }

    Log.d(PLATFORM_TAG, "=========================================")
}

actual fun answerCall() {
    Log.d(PLATFORM_TAG, "============ ANSWER CALL REQUEST ============")
    Log.d(PLATFORM_TAG, "Timestamp: ${System.currentTimeMillis()}")

    try {
        val telecomManager = applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val hasPermission = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ANSWER_PHONE_CALLS
            ) == PackageManager.PERMISSION_GRANTED

            Log.d(PLATFORM_TAG, "Has ANSWER_PHONE_CALLS permission: $hasPermission")
            Log.d(PLATFORM_TAG, "Android version supports acceptRingingCall(): ${Build.VERSION.SDK_INT >= Build.VERSION_CODES.O}")

            if (hasPermission) {
                telecomManager.acceptRingingCall()
                Log.d(PLATFORM_TAG, "TelecomManager.acceptRingingCall() executed")
            } else {
                Log.w(PLATFORM_TAG, "Missing ANSWER_PHONE_CALLS permission")
            }
        } else {
            Log.w(PLATFORM_TAG, "acceptRingingCall() not supported on Android < O (${Build.VERSION.SDK_INT})")
        }

        // Also notify our call manager
        voxity.org.dialer.managers.CallManager.getInstance().answerCall()
        Log.d(PLATFORM_TAG, "CallManager.answerCall() notified")

    } catch (e: Exception) {
        Log.e(PLATFORM_TAG, "============ ANSWER CALL ERROR ============")
        Log.e(PLATFORM_TAG, "Error answering call: ${e.message}")
        Log.e(PLATFORM_TAG, "Exception type: ${e.javaClass.simpleName}")
        Log.e(PLATFORM_TAG, "==========================================")
    }

    Log.d(PLATFORM_TAG, "============================================")
}

actual fun rejectCall() {
    Log.d(PLATFORM_TAG, "============ REJECT CALL REQUEST ============")
    Log.d(PLATFORM_TAG, "Using endCall() implementation for reject")
    Log.d(PLATFORM_TAG, "Timestamp: ${System.currentTimeMillis()}")

    endCall() // For now, reject is same as end call

    Log.d(PLATFORM_TAG, "============================================")
}

actual fun requestDefaultDialerRole() {
    Log.d(PLATFORM_TAG, "============ REQUEST DEFAULT DIALER ROLE ============")
    Log.d(PLATFORM_TAG, "Android version: ${Build.VERSION.SDK_INT}")
    Log.d(PLATFORM_TAG, "Package name: ${applicationContext.packageName}")
    Log.d(PLATFORM_TAG, "Timestamp: ${System.currentTimeMillis()}")

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d(PLATFORM_TAG, "Using RoleManager (Android Q+)")

            val roleManager = applicationContext.getSystemService(Context.ROLE_SERVICE) as RoleManager
            val isRoleAvailable = roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)
            val isRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_DIALER)

            Log.d(PLATFORM_TAG, "ROLE_DIALER available: $isRoleAvailable")
            Log.d(PLATFORM_TAG, "ROLE_DIALER currently held: $isRoleHeld")

            if (isRoleAvailable) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                Log.d(PLATFORM_TAG, "Starting role request intent")
                applicationContext.startActivity(intent)
            } else {
                Log.w(PLATFORM_TAG, "ROLE_DIALER not available on this device")
            }
        } else {
            Log.d(PLATFORM_TAG, "Using TelecomManager.ACTION_CHANGE_DEFAULT_DIALER (Android < Q)")

            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                    applicationContext.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            Log.d(PLATFORM_TAG, "Starting default dialer change intent")
            applicationContext.startActivity(intent)
        }

    } catch (e: Exception) {
        Log.e(PLATFORM_TAG, "============ REQUEST DIALER ROLE ERROR ============")
        Log.e(PLATFORM_TAG, "Error requesting default dialer role: ${e.message}")
        Log.e(PLATFORM_TAG, "Exception type: ${e.javaClass.simpleName}")
        Log.e(PLATFORM_TAG, "==================================================")
    }

    Log.d(PLATFORM_TAG, "===================================================")
}

actual fun hasCallPermissions(): Boolean {
    Log.d(PLATFORM_TAG, "============ CHECK CALL PERMISSIONS ============")

    val permissions = arrayOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.MANAGE_OWN_CALLS
    )

    val permissionStatus = permissions.map { permission ->
        val granted = ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED
        Log.d(PLATFORM_TAG, "$permission: ${if (granted) "GRANTED" else "DENIED"}")
        permission to granted
    }

    val allGranted = permissionStatus.all { it.second }
    Log.d(PLATFORM_TAG, "All required permissions granted: $allGranted")
    Log.d(PLATFORM_TAG, "===============================================")

    return allGranted
}

actual fun requestCallPermissions() {
    Log.d(PLATFORM_TAG, "============ REQUEST CALL PERMISSIONS ============")
    Log.d(PLATFORM_TAG, "This should be called from MainActivity")
    Log.d(PLATFORM_TAG, "Timestamp: ${System.currentTimeMillis()}")
    Log.d(PLATFORM_TAG, "=================================================")
}

private fun logCallAttempt(phoneNumber: String, method: String, extras: Bundle?) {
    val metadata = JSONObject().apply {
        put("event", "CALL_ATTEMPT")
        put("phoneNumber", phoneNumber)
        put("method", method)
        put("timestamp", System.currentTimeMillis())
        put("androidVersion", Build.VERSION.SDK_INT)
        put("hasCallPermission", ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED)
        extras?.let {
            put("hasExtras", true)
            put("extrasKeys", it.keySet().joinToString(","))
        } ?: put("hasExtras", false)
    }
    Log.d("CallAttemptMetadata", metadata.toString())
}