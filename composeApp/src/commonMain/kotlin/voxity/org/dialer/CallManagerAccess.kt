package voxity.org.dialer

interface CallManagerAccess {
    fun muteCall(mute: Boolean)
    fun holdCall()
    fun unholdCall()
    fun endCall()
    fun answerCall()
    fun rejectCall()
}

expect fun getCallManagerAccess(): CallManagerAccess