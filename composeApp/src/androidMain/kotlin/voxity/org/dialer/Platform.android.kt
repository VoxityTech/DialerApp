package voxity.org.dialer

import android.Manifest
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

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

private lateinit var applicationContext: Context
private val PLATFORM_TAG = "PlatformCalls"

fun initializeContext(context: Context) {
    applicationContext = context.applicationContext
}

actual fun makeCall(phoneNumber: String) {

    try {
        val telecomManager = applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        // Check if we have permission and are default dialer
        val hasCallPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCallPermission) {
            val uri = Uri.fromParts("tel", phoneNumber, null)
            val extras = Bundle().apply {
                putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false)
                putLong("CALL_REQUEST_TIMESTAMP", System.currentTimeMillis())
            }

            telecomManager.placeCall(uri, extras)

        } else {

            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            applicationContext.startActivity(callIntent)
        }

    } catch (e: Exception) {

        Toast.makeText(applicationContext, "Error making call: ${e.message}", Toast.LENGTH_SHORT).show()
    }

}

actual fun endCall() {

    try {
        val telecomManager = applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val hasPermission = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ANSWER_PHONE_CALLS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                telecomManager.endCall()
            } else {
                Log.w(PLATFORM_TAG, "Missing ANSWER_PHONE_CALLS permission")
            }
        } else {
            Log.w(PLATFORM_TAG, "endCall() not supported on Android < P (${Build.VERSION.SDK_INT})")
        }

        // Also notify our call manager
        voxity.org.dialer.managers.CallManager.getInstance().endCall()

    } catch (e: Exception) {
        Log.e(PLATFORM_TAG, "============ END CALL ERROR ============")
  }

}

actual fun answerCall() {

    try {
        val telecomManager = applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val hasPermission = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ANSWER_PHONE_CALLS
            ) == PackageManager.PERMISSION_GRANTED

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

    } catch (e: Exception) {
        Log.e(PLATFORM_TAG, "============ ANSWER CALL ERROR ============")
    }

}

actual fun rejectCall() {

    endCall()
}

