package voxity.org.dialer.services

import android.net.Uri
import android.os.Bundle
import android.telecom.*
import android.util.Log
import voxity.org.dialer.managers.CallManager
import org.json.JSONObject

class MyConnectionService : ConnectionService() {

    private val TAG = "MyConnectionService"
    private val CONNECTION_TAG = "ConnectionMetadata"
    private val callManager by lazy { CallManager.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "============ CONNECTION SERVICE CREATED ============")
        Log.d(TAG, "Service created at: ${System.currentTimeMillis()}")
        Log.d(TAG, "==================================================")
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection? {
        Log.d(TAG, "========== CONNECTION REQUEST DETAILS ==========")

        // Log ALL request extras
        val extras = request?.extras
        if (extras != null) {
            Log.d(TAG, "Request Extra Keys: ${extras.keySet()}")
            for (key in extras.keySet()) {
                val value = extras.get(key)
                Log.d(TAG, "Request Extra [$key]: $value")

                // Look for bridge/channel related keys
                if (key.contains("bridge", ignoreCase = true) ||
                    key.contains("channel", ignoreCase = true)) {
                    Log.w(TAG, "POTENTIAL BRIDGE/CHANNEL DATA: [$key]: $value")
                }
            }
        }

        // Log the address details
        Log.d(TAG, "Request Address: ${request?.address}")
        Log.d(TAG, "Request Address Scheme: ${request?.address?.scheme}")
        Log.d(TAG, "Request Address Path: ${request?.address?.path}")
        Log.d(TAG, "Request Address Query: ${request?.address?.query}")
        Log.d(TAG, "Request Address Fragment: ${request?.address?.fragment}")

        Log.d(TAG, "============================================")

        val connection = MyConnection(request?.address, false, request?.extras)
        connection.setInitializing()

        logConnectionCreation(connection, "OUTGOING")
        callManager.addConnection(connection)

        Log.d(TAG, "Outgoing connection created successfully")
        Log.d(TAG, "==================================================")
        return connection
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection? {
        Log.d(TAG, "============ CREATE INCOMING CONNECTION ============")
        Log.d(TAG, "Phone Account: $connectionManagerPhoneAccount")
        Log.d(TAG, "Request Address: ${request?.address}")
        Log.d(TAG, "Request Extras: ${request?.extras}")
        Log.d(TAG, "Timestamp: ${System.currentTimeMillis()}")

        val connection = MyConnection(request?.address, true, request?.extras)
        connection.setRinging()

        logConnectionCreation(connection, "INCOMING")
        callManager.addConnection(connection)

        Log.d(TAG, "Incoming connection created successfully")
        Log.d(TAG, "==================================================")
        return connection
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
        Log.e(TAG, "============ INCOMING CONNECTION FAILED ============")
        Log.e(TAG, "Phone Account: $connectionManagerPhoneAccount")
        Log.e(TAG, "Request: ${request?.address}")
        Log.e(TAG, "Timestamp: ${System.currentTimeMillis()}")
        Log.e(TAG, "==================================================")
    }

    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
        Log.e(TAG, "============ OUTGOING CONNECTION FAILED ============")
        Log.e(TAG, "Phone Account: $connectionManagerPhoneAccount")
        Log.e(TAG, "Request: ${request?.address}")
        Log.e(TAG, "Timestamp: ${System.currentTimeMillis()}")
        Log.e(TAG, "==================================================")
    }

    inner class MyConnection(
        private val address: Uri?,
        private val isIncoming: Boolean,
        private val requestExtras: Bundle?
    ) : Connection() {

        private val connectionId = System.currentTimeMillis()

        init {
            Log.d(CONNECTION_TAG, "============ CONNECTION INIT ============")
            Log.d(CONNECTION_TAG, "Connection ID: $connectionId")
            Log.d(CONNECTION_TAG, "Address: $address")
            Log.d(CONNECTION_TAG, "Is Incoming: $isIncoming")
            Log.d(CONNECTION_TAG, "Request Extras: $requestExtras")

            connectionCapabilities = CAPABILITY_SUPPORT_HOLD or CAPABILITY_HOLD
            audioModeIsVoip = false

            if (isIncoming) {
                Log.d(CONNECTION_TAG, "Setting connection as RINGING")
                setRinging()
            } else {
                Log.d(CONNECTION_TAG, "Setting connection as DIALING")
                setDialing()
            }

            Log.d(CONNECTION_TAG, "Connection initialized with capabilities: $connectionCapabilities")
            Log.d(CONNECTION_TAG, "=======================================")
        }

        override fun onAnswer() {
            Log.d(CONNECTION_TAG, "============ CONNECTION ANSWER ============")
            Log.d(CONNECTION_TAG, "Connection ID: $connectionId")
            Log.d(CONNECTION_TAG, "Address: ${address?.schemeSpecificPart}")
            Log.d(CONNECTION_TAG, "Timestamp: ${System.currentTimeMillis()}")

            logConnectionAction("ANSWER", "Setting connection as ACTIVE")
            setActive()

            Log.d(CONNECTION_TAG, "Connection answered and set to ACTIVE")
            Log.d(CONNECTION_TAG, "=========================================")
        }

        override fun onAnswer(videoState: Int) {
            Log.d(CONNECTION_TAG, "============ CONNECTION ANSWER WITH VIDEO ============")
            Log.d(CONNECTION_TAG, "Connection ID: $connectionId")
            Log.d(CONNECTION_TAG, "Address: ${address?.schemeSpecificPart}")
            Log.d(CONNECTION_TAG, "Video State: $videoState")
            Log.d(CONNECTION_TAG, "Timestamp: ${System.currentTimeMillis()}")

            logConnectionAction("ANSWER_VIDEO", "Setting connection as ACTIVE with video")
            setActive()

            Log.d(CONNECTION_TAG, "Connection answered with video and set to ACTIVE")
            Log.d(CONNECTION_TAG, "====================================================")
        }

        override fun onReject() {
            Log.d(CONNECTION_TAG, "============ CONNECTION REJECT ============")
            Log.d(CONNECTION_TAG, "Connection ID: $connectionId")
            Log.d(CONNECTION_TAG, "Address: ${address?.schemeSpecificPart}")
            Log.d(CONNECTION_TAG, "Timestamp: ${System.currentTimeMillis()}")

            logConnectionAction("REJECT", "Disconnecting with REJECTED cause")
            setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
            destroy()

            Log.d(CONNECTION_TAG, "Connection rejected and destroyed")
            Log.d(CONNECTION_TAG, "=========================================")
        }

        override fun onReject(rejectReason: Int) {
            Log.d(CONNECTION_TAG, "============ CONNECTION REJECT WITH REASON ============")
            Log.d(CONNECTION_TAG, "Connection ID: $connectionId")
            Log.d(CONNECTION_TAG, "Address: ${address?.schemeSpecificPart}")
            Log.d(CONNECTION_TAG, "Reject Reason: $rejectReason")
            Log.d(CONNECTION_TAG, "Timestamp: ${System.currentTimeMillis()}")

            logConnectionAction("REJECT_WITH_REASON", "Rejecting with reason: $rejectReason")
            setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
            destroy()

            Log.d(CONNECTION_TAG, "Connection rejected with reason and destroyed")
            Log.d(CONNECTION_TAG, "=====================================================")
        }

        override fun onDisconnect() {
            Log.d(CONNECTION_TAG, "============ CONNECTION DISCONNECT ============")
            Log.d(CONNECTION_TAG, "Connection ID: $connectionId")
            Log.d(CONNECTION_TAG, "Address: ${address?.schemeSpecificPart}")
            Log.d(CONNECTION_TAG, "Current State: $state")
            Log.d(CONNECTION_TAG, "Timestamp: ${System.currentTimeMillis()}")

            logConnectionAction("DISCONNECT", "Disconnecting with LOCAL cause")
            setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
            destroy()

            Log.d(CONNECTION_TAG, "Connection disconnected and destroyed")
            Log.d(CONNECTION_TAG, "=============================================")
        }

        override fun onAbort() {
            Log.d(CONNECTION_TAG, "============ CONNECTION ABORT ============")
            Log.d(CONNECTION_TAG, "Connection ID: $connectionId")
            Log.d(CONNECTION_TAG, "Address: ${address?.schemeSpecificPart}")
            Log.d(CONNECTION_TAG, "Timestamp: ${System.currentTimeMillis()}")

            logConnectionAction("ABORT", "Disconnecting with CANCELED cause")
            setDisconnected(DisconnectCause(DisconnectCause.CANCELED))
            destroy()

            Log.d(CONNECTION_TAG, "Connection aborted and destroyed")
            Log.d(CONNECTION_TAG, "========================================")
        }

        override fun onHold() {
            Log.d(CONNECTION_TAG, "============ CONNECTION HOLD ============")
            Log.d(CONNECTION_TAG, "Connection ID: $connectionId")
            Log.d(CONNECTION_TAG, "Address: ${address?.schemeSpecificPart}")
            Log.d(CONNECTION_TAG, "Previous State: $state")
            Log.d(CONNECTION_TAG, "Timestamp: ${System.currentTimeMillis()}")

            logConnectionAction("HOLD", "Setting connection on hold")
            setOnHold()

            Log.d(CONNECTION_TAG, "Connection set on hold")
            Log.d(CONNECTION_TAG, "=======================================")
        }

        override fun onUnhold() {
            Log.d(CONNECTION_TAG, "============ CONNECTION UNHOLD ============")
            Log.d(CONNECTION_TAG, "Connection ID: $connectionId")
            Log.d(CONNECTION_TAG, "Address: ${address?.schemeSpecificPart}")
            Log.d(CONNECTION_TAG, "Previous State: $state")
            Log.d(CONNECTION_TAG, "Timestamp: ${System.currentTimeMillis()}")

            logConnectionAction("UNHOLD", "Setting connection as active")
            setActive()

            Log.d(CONNECTION_TAG, "Connection unheld and set to active")
            Log.d(CONNECTION_TAG, "=========================================")
        }

        override fun onPlayDtmfTone(c: Char) {
            Log.d(CONNECTION_TAG, "============ DTMF TONE PLAY ============")
            Log.d(CONNECTION_TAG, "Connection ID: $connectionId")
            Log.d(CONNECTION_TAG, "DTMF Tone: $c")
            Log.d(CONNECTION_TAG, "Timestamp: ${System.currentTimeMillis()}")
            Log.d(CONNECTION_TAG, "======================================")
        }

        override fun onStopDtmfTone() {
            Log.d(CONNECTION_TAG, "============ DTMF TONE STOP ============")
            Log.d(CONNECTION_TAG, "Connection ID: $connectionId")
            Log.d(CONNECTION_TAG, "DTMF tone stopped")
            Log.d(CONNECTION_TAG, "Timestamp: ${System.currentTimeMillis()}")
            Log.d(CONNECTION_TAG, "======================================")
        }

        override fun onStateChanged(state: Int) {
            super.onStateChanged(state)
            Log.d(CONNECTION_TAG, "============ CONNECTION STATE CHANGED ============")
            Log.d(CONNECTION_TAG, "Connection ID: $connectionId")
            Log.d(CONNECTION_TAG, "Address: ${address?.schemeSpecificPart}")
            Log.d(CONNECTION_TAG, "New State: ${getConnectionStateString(state)}")
            Log.d(CONNECTION_TAG, "Timestamp: ${System.currentTimeMillis()}")

            callManager.updateConnectionState(this, state)

            Log.d(CONNECTION_TAG, "State change processed")
            Log.d(CONNECTION_TAG, "================================================")
        }

        override fun onPostDialContinue(proceed: Boolean) {
            Log.d(CONNECTION_TAG, "============ POST DIAL CONTINUE ============")
            Log.d(CONNECTION_TAG, "Connection ID: $connectionId")
            Log.d(CONNECTION_TAG, "Proceed: $proceed")
            Log.d(CONNECTION_TAG, "Timestamp: ${System.currentTimeMillis()}")
            Log.d(CONNECTION_TAG, "===========================================")
        }

        private fun logConnectionAction(action: String, description: String) {
            val metadata = JSONObject().apply {
                put("connectionId", connectionId)
                put("action", action)
                put("description", description)
                put("address", address?.schemeSpecificPart ?: "Unknown")
                put("isIncoming", isIncoming)
                put("currentState", getConnectionStateString(state))
                put("capabilities", connectionCapabilities)
                put("properties", connectionProperties)
                put("timestamp", System.currentTimeMillis())
            }
            Log.d("ConnectionActionMetadata", metadata.toString())
        }

        private fun getConnectionStateString(state: Int): String {
            return when (state) {
                Connection.STATE_INITIALIZING -> "INITIALIZING"
                Connection.STATE_NEW -> "NEW"
                Connection.STATE_RINGING -> "RINGING"
                Connection.STATE_DIALING -> "DIALING"
                Connection.STATE_ACTIVE -> "ACTIVE"
                Connection.STATE_HOLDING -> "HOLDING"
                Connection.STATE_DISCONNECTED -> "DISCONNECTED"
                else -> "UNKNOWN($state)"
            }
        }
    }

    private fun logConnectionCreation(connection: Connection, type: String) {
        val metadata = JSONObject().apply {
            put("event", "CONNECTION_CREATED")
            put("type", type)
            put("address", connection.address?.schemeSpecificPart ?: "Unknown")
            put("state", connection.state)
            put("capabilities", connection.connectionCapabilities)
            put("properties", connection.connectionProperties)
            put("audioModeVoip", connection.audioModeIsVoip)
            put("timestamp", System.currentTimeMillis())
        }
        Log.d("ConnectionCreationMetadata", metadata.toString())
    }
}