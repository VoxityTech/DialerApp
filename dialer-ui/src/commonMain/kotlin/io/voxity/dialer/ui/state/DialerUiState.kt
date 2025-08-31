package io.voxity.dialer.ui.state

import io.voxity.dialer.domain.models.CallHistoryItem
import io.voxity.dialer.domain.models.CallState
import io.voxity.dialer.domain.models.Contact
import io.voxity.dialer.ui.navigation.NavigationItem

// Pure state data classes - no business logic
data class ContactsScreenState(
    val contacts: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filteredContacts: List<Contact> = emptyList()
)

data class CallHistoryScreenState(
    val callHistory: List<CallHistoryItem> = emptyList(),
    val isLoading: Boolean = false
)

data class DialerScreenState(
    val phoneNumber: String = "",
    val canMakeCall: Boolean = false
)

data class ActiveCallScreenState(
    val callState: CallState = CallState(),
    val callDuration: Long = 0L,
    val showAudioSelector: Boolean = false
)

// Navigation state
data class DialerNavigationState(
    val selectedTab: Any = DefaultScreens.CallHistory,
    val showHistory: Boolean = true,
    val showContacts: Boolean = true,
    val showDialerModal: Boolean = false,
    val isDefaultDialer: Boolean = false,
    val additionalScreens: List<NavigationItem> = emptyList()
)

// Default screen identifiers
enum class DefaultScreens {
    CallHistory,
    Contacts,
    Dialer
}