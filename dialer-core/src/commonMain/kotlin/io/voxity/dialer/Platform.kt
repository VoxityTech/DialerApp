package io.voxity.dialer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun makeCall(phoneNumber: String)
expect fun endCall()
expect fun answerCall()
expect fun rejectCall()