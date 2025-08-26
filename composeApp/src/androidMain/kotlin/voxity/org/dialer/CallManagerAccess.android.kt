package voxity.org.dialer

import voxity.org.dialer.managers.CallManager

actual fun getCallManagerAccess(): CallManagerAccess = AndroidCallManagerAccess()

class AndroidCallManagerAccess : CallManagerAccess {
    private val callManager = CallManager.getInstance()

    override fun muteCall(mute: Boolean) = callManager.muteCall(mute)
    override fun holdCall() = callManager.holdCall()
    override fun unholdCall() = callManager.unholdCall()
    override fun endCall() = callManager.endCall()
    override fun answerCall() = callManager.answerCall()
    override fun rejectCall() = callManager.rejectCall()
}