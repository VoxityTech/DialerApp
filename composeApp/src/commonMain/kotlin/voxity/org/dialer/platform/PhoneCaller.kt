// shared/src/commonMain/kotlin/voxity/org/dialer/platform/PhoneCaller.kt
package voxity.org.dialer.platform

expect class PhoneCaller {
    fun initiateCall(phoneNumber: String)
}
