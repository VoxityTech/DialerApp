package io.voxity.dialer.data

import android.content.Context
import android.provider.CallLog
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

data class CallLogItem(
    val phoneNumber: String,
    val contactName: String,
    val callType: String,
    val date: String,
    val duration: Long
)

class CallLogReader(private val context: Context) {

    fun getCallHistory(): List<CallLogItem> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
            != PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }

        val callLog = mutableListOf<CallLogItem>()

        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            ),
            null,
            null,
            "${CallLog.Calls.DATE} DESC"
        )

        cursor?.use {
            val numberIndex = it.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val nameIndex = it.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
            val typeIndex = it.getColumnIndexOrThrow(CallLog.Calls.TYPE)
            val dateIndex = it.getColumnIndexOrThrow(CallLog.Calls.DATE)
            val durationIndex = it.getColumnIndexOrThrow(CallLog.Calls.DURATION)

            while (it.moveToNext()) {
                val number = it.getString(numberIndex) ?: ""
                val name = it.getString(nameIndex) ?: ""
                val type = it.getInt(typeIndex)
                val dateTimestamp = it.getLong(dateIndex)
                val duration = it.getLong(durationIndex)

                val callType = when (type) {
                    CallLog.Calls.INCOMING_TYPE -> "INCOMING"
                    CallLog.Calls.OUTGOING_TYPE -> "OUTGOING"
                    CallLog.Calls.MISSED_TYPE -> "MISSED"
                    else -> "UNKNOWN"
                }

                callLog.add(
                    CallLogItem(
                        phoneNumber = number,
                        contactName = name,
                        callType = callType,
                        date = dateTimestamp.toString(),
                        duration = duration
                    )
                )
            }
        }

        return callLog
    }
}