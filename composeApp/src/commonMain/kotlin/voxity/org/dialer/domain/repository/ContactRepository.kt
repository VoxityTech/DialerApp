package voxity.org.dialer.domain.repository


import voxity.org.dialer.domain.models.Contact

interface ContactRepository {
    suspend fun getContacts(): List<Contact>
}