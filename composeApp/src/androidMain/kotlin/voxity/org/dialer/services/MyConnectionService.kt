package voxity.org.dialer.services

import android.net.Uri
import android.os.Bundle
import android.telecom.*
import voxity.org.dialer.managers.CallManager

class MyConnectionService : ConnectionService() {

    private val callManager by lazy { CallManager.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection? {

        val connection = MyConnection(request?.address, false, request?.extras)
        connection.setInitializing()

        callManager.addConnection(connection) // This should work now
        return connection
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection? {

        val connection = MyConnection(request?.address, true, request?.extras)
        connection.setRinging()
        callManager.addConnection(connection)
        return connection
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
    }

    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
    }

    inner class MyConnection(
        private val address: Uri?,
        private val isIncoming: Boolean,
        private val requestExtras: Bundle?
    ) : Connection() {

        init {
            connectionCapabilities = CAPABILITY_SUPPORT_HOLD or CAPABILITY_HOLD
            audioModeIsVoip = false

            if (isIncoming) {
                setRinging()
            } else {
                setDialing()
            }
        }

        override fun onAnswer() {
            setActive()
        }

        override fun onAnswer(videoState: Int) {
            setActive()
        }

        override fun onReject() {
            setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
            destroy()
        }

        override fun onReject(rejectReason: Int) {
            setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
            destroy()
        }

        override fun onDisconnect() {
            setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
            destroy()
        }

        override fun onAbort() {
            setDisconnected(DisconnectCause(DisconnectCause.CANCELED))
            destroy()
        }

        override fun onHold() {
            setOnHold()
        }

        override fun onUnhold() {
            setActive()
        }

        override fun onPlayDtmfTone(c: Char) {
        }

        override fun onStopDtmfTone() {
        }

        override fun onStateChanged(state: Int) {
            super.onStateChanged(state)
            callManager.updateConnectionState()
        }

        override fun onPostDialContinue(proceed: Boolean) {
        }
    }
}