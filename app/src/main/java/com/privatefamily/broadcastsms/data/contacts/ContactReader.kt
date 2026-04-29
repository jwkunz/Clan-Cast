package com.privatefamily.broadcastsms.data.contacts

import android.content.Context
import android.content.ContentResolver
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactReader(
    private val context: Context,
    private val contentResolver: ContentResolver,
    private val phoneNumberNormalizer: PhoneNumberNormalizer,
) {
    suspend fun readContacts(): List<DeviceContact> = withContext(Dispatchers.IO) {
        val contacts = linkedMapOf<String, MutableContact>()
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL,
            ),
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} COLLATE NOCASE ASC",
        )

        cursor?.use { rows ->
            val contactIdIndex = rows.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val displayNameIndex = rows.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIdIndex = rows.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID)
            val numberIndex = rows.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val typeIndex = rows.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)
            val labelIndex = rows.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.LABEL)

            while (rows.moveToNext()) {
                val contactId = rows.getString(contactIdIndex)
                val displayName = rows.getString(displayNameIndex).orEmpty()
                val number = rows.getString(numberIndex).orEmpty()
                val type = rows.getInt(typeIndex)
                val customLabel = rows.getString(labelIndex)
                val typeLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                    context.resources,
                    type,
                    customLabel,
                ).toString()

                val mutable = contacts.getOrPut(contactId) {
                    val parts = parseName(displayName)
                    MutableContact(
                        contactId = contactId,
                        displayName = displayName,
                        firstName = parts.first,
                        lastName = parts.second,
                    )
                }
                mutable.phoneNumbers += DevicePhoneNumber(
                    phoneNumberId = rows.getString(phoneIdIndex),
                    number = number,
                    normalizedNumber = phoneNumberNormalizer.normalize(number),
                    typeLabel = typeLabel,
                )
            }
        }

        contacts.values.map { it.toDeviceContact() }
    }

    private fun parseName(displayName: String): Pair<String, String> {
        val parts = displayName.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "" to ""
            parts.size == 1 -> parts.first() to ""
            else -> parts.first() to parts.drop(1).joinToString(" ")
        }
    }

    private data class MutableContact(
        val contactId: String,
        val displayName: String,
        val firstName: String,
        val lastName: String,
        val phoneNumbers: MutableList<DevicePhoneNumber> = mutableListOf(),
    ) {
        fun toDeviceContact(): DeviceContact = DeviceContact(
            contactId = contactId,
            displayName = displayName,
            firstName = firstName,
            lastName = lastName,
            phoneNumbers = phoneNumbers,
        )
    }
}
