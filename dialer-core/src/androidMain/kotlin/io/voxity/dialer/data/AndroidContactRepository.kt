package io.voxity.dialer.data

import io.voxity.dialer.domain.repository.ContactRepository
import io.voxity.dialer.domain.models.Contact

class AndroidContactRepository(private val contactsReader: ContactsReader) : ContactRepository {
    override suspend fun getContacts(): List<Contact> {
        return contactsReader.getContacts().map { androidContact ->
            Contact(
                name = androidContact.name,
                phoneNumbers = androidContact.phoneNumbers
            )
        }
    }
}