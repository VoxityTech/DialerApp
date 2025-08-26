package voxity.org.dialer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun makeCall(phoneNumber: String)
expect fun endCall()
expect fun answerCall()
expect fun rejectCall()
expect fun requestDefaultDialerRole()
expect fun hasCallPermissions(): Boolean
expect fun requestCallPermissions()