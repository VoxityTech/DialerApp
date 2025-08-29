package io.voxity.dialer.ui.callbacks

import io.voxity.dialer.domain.models.Contact

// Pure callback interfaces
interface ContactsScreenCallbacks {
    fun onContactSelected(contact: Contact)
    fun onCallContact(phoneNumber: String)
    fun onSearchQueryChanged(query: String)
    fun onBlockNumber(phoneNumber: String) {}
    fun onUnblockNumber(phoneNumber: String) {}
}

interface CallHistoryScreenCallbacks {
    fun onCallHistoryItemClicked(phoneNumber: String)
    fun onRefresh() {}
}

interface DialerScreenCallbacks {
    fun onNumberChanged(number: String)
    fun onMakeCall(phoneNumber: String)
    fun onDeleteDigit()
    fun onClearNumber()
}

interface ActiveCallScreenCallbacks {
    fun onAnswerCall()
    fun onRejectCall()
    fun onEndCall()
    fun onHoldCall()
    fun onUnholdCall()
    fun onMuteCall(muted: Boolean)
    fun onShowAudioSelector()
    fun onHideAudioSelector()
}

interface DialerNavigationCallbacks {
    fun onTabSelected(screenId: Any)
    fun onShowDialerModal()
    fun onHideDialerModal()
    fun onRequestDefaultDialer()
    fun onRequestPermissions()
}