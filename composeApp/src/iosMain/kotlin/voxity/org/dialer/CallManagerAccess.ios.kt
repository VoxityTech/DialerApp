package voxity.org.dialer

actual fun getCallManagerAccess(): CallManagerAccess = IOSCallManagerAccess()

class IOSCallManagerAccess : CallManagerAccess {
    override fun muteCall(mute: Boolean) {
        println("iOS: Mute call $mute")
    }

    override fun holdCall() {
        println("iOS: Hold call")
    }

    override fun unholdCall() {
        println("iOS: Unhold call")
    }

    override fun endCall() {
        println("iOS: End call")
    }

    override fun answerCall() {
        println("iOS: Answer call")
    }

    override fun rejectCall() {
        println("iOS: Reject call")
    }
}