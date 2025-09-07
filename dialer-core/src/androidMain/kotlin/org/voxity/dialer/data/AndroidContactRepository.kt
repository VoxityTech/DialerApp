package org.voxity.dialer.data

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import org.voxity.dialer.domain.repository.ContactRepository
import org.voxity.dialer.domain.models.Contact
import org.voxity.dialer.domain.models.CallResult
import android.content.ContentUris

class AndroidContactRepository(
    private val contactsReader: ContactsReader
) : ContactRepository {

    override suspend fun getContacts(): List<Contact> {
        return contactsReader.getContacts().map { androidContact ->
            Contact(
                name = androidContact.name,
                phoneNumbers = androidContact.phoneNumbers
            )
        }
    }

    override suspend fun saveContact(name: String, phoneNumber: String): CallResult {
        return try {
            if (ContextCompat.checkSelfPermission(
                    contactsReader.context,
                    Manifest.permission.WRITE_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED) {
                return CallResult.Error("Write contacts permission required")
            }

            val values = ContentValues().apply {
                put(ContactsContract.RawContacts.ACCOUNT_TYPE, null as String?)
                put(ContactsContract.RawContacts.ACCOUNT_NAME, null as String?)
            }

            val rawContactUri = contactsReader.context.contentResolver.insert(
                ContactsContract.RawContacts.CONTENT_URI, values
            ) ?: return CallResult.Error("Failed to create contact")

            val rawContactId = ContentUris.parseId(rawContactUri)

            // Add name
            val nameValues = ContentValues().apply {
                put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
            }
            contactsReader.context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, nameValues)

            // Add phone number
            val phoneValues = ContentValues().apply {
                put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
            }
            contactsReader.context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, phoneValues)

            CallResult.Success
        } catch (e: Exception) {
            CallResult.Error("Failed to save contact: ${e.message}")
        }
    }

    override suspend fun updateContact(contact: Contact): CallResult {
        return CallResult.Error("Update not implemented yet")
    }

    override suspend fun deleteContact(contactId: String): CallResult {
        return CallResult.Error("Delete not implemented yet")
    }
}