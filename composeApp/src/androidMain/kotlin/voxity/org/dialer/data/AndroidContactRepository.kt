package voxity.org.dialer.data

import voxity.org.dialer.domain.repository.ContactRepository
import voxity.org.dialer.domain.models.Contact

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