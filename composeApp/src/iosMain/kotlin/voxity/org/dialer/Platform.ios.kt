package voxity.org.dialer

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun makeCall(phoneNumber: String) {
}

actual fun endCall() {
}

actual fun answerCall() {
}

actual fun rejectCall() {
}
