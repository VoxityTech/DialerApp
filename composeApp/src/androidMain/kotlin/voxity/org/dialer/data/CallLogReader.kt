package voxity.org.dialer.data

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
        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

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
            while (it.moveToNext()) {
                val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER)) ?: ""
                val name = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)) ?: ""
                val type = it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                val date = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
                val duration = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))

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
                        date = dateFormat.format(Date(date)),
                        duration = duration
                    )
                )
            }
        }

        return callLog
    }
}