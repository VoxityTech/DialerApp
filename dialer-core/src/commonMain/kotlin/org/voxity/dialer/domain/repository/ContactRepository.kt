package org.voxity.dialer.domain.repository


import org.voxity.dialer.domain.models.CallResult
import org.voxity.dialer.domain.models.Contact

interface ContactRepository {
    suspend fun getContacts(): List<Contact>
    suspend fun saveContact(name: String, phoneNumber: String): CallResult
    suspend fun updateContact(contact: Contact): CallResult
    suspend fun deleteContact(contactId: String): CallResult
}