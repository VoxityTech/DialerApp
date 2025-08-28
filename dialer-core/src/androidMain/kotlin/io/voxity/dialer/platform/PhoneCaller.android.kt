package io.voxity.dialer.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.core.content.ContextCompat

actual class PhoneCaller(private val context: Context) {
    actual fun initiateCall(phoneNumber: String) {
        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

            val hasCallPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED

            if (hasCallPermission) {
                val uri = Uri.fromParts("tel", phoneNumber, null)
                val extras = Bundle().apply {
                    putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false)
                    putBoolean("FROM_DIALER_APP", true)
                }

                telecomManager.placeCall(uri, extras)
            } else {
                Toast.makeText(context, "Call permission required", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(context, "Error placing call: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}