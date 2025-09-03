package org.voxity.dialer.domain.models

data class Contact(
    val name: String,
    val phoneNumbers: List<String>
)