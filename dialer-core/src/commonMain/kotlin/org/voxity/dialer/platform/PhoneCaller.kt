package org.voxity.dialer.platform

expect class PhoneCaller {
    fun initiateCall(phoneNumber: String)
}