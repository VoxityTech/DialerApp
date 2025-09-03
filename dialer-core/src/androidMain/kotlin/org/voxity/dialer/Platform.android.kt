package org.voxity.dialer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.content.ContextCompat
import org.voxity.dialer.domain.models.CallResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

object PlatformCallManager : KoinComponent {
    private val context: Context by inject()

    suspend fun makeCallInternal(phoneNumber: String): CallResult = withContext(Dispatchers.Main) {
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
                    putLong("CALL_REQUEST_TIMESTAMP", System.currentTimeMillis())
                }
                telecomManager.placeCall(uri, extras)
                CallResult.Success
            } else {
                try {
                    val callIntent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:$phoneNumber")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(callIntent)
                    CallResult.Success
                } catch (e: Exception) {
                    CallResult.Error("No permission and unable to start call intent", e)
                }
            }
        } catch (e: Exception) {
            CallResult.Error("Failed to make call", e)
        }
    }

    suspend fun endCallInternal(): CallResult = withContext(Dispatchers.Main) {
        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ANSWER_PHONE_CALLS
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    telecomManager.endCall()
                } else {
                    Log.w("PlatformCalls", "Missing ANSWER_PHONE_CALLS permission")
                }
            }
            CallResult.Success
        } catch (e: Exception) {
            CallResult.Error("Failed to end call", e)
        }
    }
}

actual suspend fun makeCall(phoneNumber: String): CallResult =
    PlatformCallManager.makeCallInternal(phoneNumber)

actual suspend fun endCall(): CallResult =
    PlatformCallManager.endCallInternal()

actual suspend fun answerCall(): CallResult = withContext(Dispatchers.Main) {
    try {
        val context: Context by PlatformCallManager.inject()
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ANSWER_PHONE_CALLS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                telecomManager.acceptRingingCall()
            } else {
                Log.w("PlatformCalls", "Missing ANSWER_PHONE_CALLS permission")
            }
        }
        CallResult.Success
    } catch (e: Exception) {
        CallResult.Error("Failed to answer call", e)
    }
}

actual suspend fun rejectCall(): CallResult = endCall()