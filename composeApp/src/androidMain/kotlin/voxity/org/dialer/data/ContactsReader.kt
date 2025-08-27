package voxity.org.dialer.data

import android.content.Context
import android.provider.ContactsContract
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

data class Contact(
    val name: String,
    val phoneNumbers: List<String>
)

class ContactsReader(private val context: Context) {

    fun getContacts(): List<Contact> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }

        val contacts = mutableListOf<Contact>()
        val contactsMap = mutableMapOf<String, MutableList<String>>()

        // Get contacts with phone numbers
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: ""
                val number = it.getString(it.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: ""

                if (name.isNotEmpty() && number.isNotEmpty()) {
                    if (!contactsMap.containsKey(name)) {
                        contactsMap[name] = mutableListOf()
                    }
                    contactsMap[name]?.add(number)
                }
            }
        }

        contactsMap.forEach { (name, numbers) ->
            contacts.add(Contact(name = name, phoneNumbers = numbers))
        }

        return contacts.sortedBy { it.name }
    }
}