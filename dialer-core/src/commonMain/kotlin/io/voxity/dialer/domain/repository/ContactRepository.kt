package io.voxity.dialer.domain.repository


import io.voxity.dialer.domain.models.Contact

interface ContactRepository {
    suspend fun getContacts(): List<Contact>
}