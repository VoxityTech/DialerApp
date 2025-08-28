package io.voxity.dialer.platform

expect class PhoneCaller {
    fun initiateCall(phoneNumber: String)
}