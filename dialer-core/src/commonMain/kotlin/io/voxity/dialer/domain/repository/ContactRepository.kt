package io.voxity.dialer.domain.repository


import io.voxity.dialer.domain.models.CallResult
import io.voxity.dialer.domain.models.Contact

interface ContactRepository {
    suspend fun getContacts(): List<Contact>
    suspend fun saveContact(name: String, phoneNumber: String): CallResult
    suspend fun updateContact(contact: Contact): CallResult
    suspend fun deleteContact(contactId: String): CallResult
}