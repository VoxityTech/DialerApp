package org.voxity.dialer.data

import android.content.Context
import android.provider.ContactsContract
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.voxity.dialer.domain.models.Contact

class ContactsReader(val context: Context) {

    fun getContacts(): List<Contact> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }

        val contacts = mutableListOf<Contact>()
        val contactsMap = mutableMapOf<String, MutableList<String>>()

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
            contacts.add(Contact
                (name = name, phoneNumbers = numbers))
        }

        return contacts.sortedBy { it.name }
    }
}