package com.privatefamily.broadcastsms.data.repository

import com.privatefamily.broadcastsms.data.contacts.ContactReader
import com.privatefamily.broadcastsms.data.contacts.DeviceContact

class ContactRepository(
    private val contactReader: ContactReader,
) {
    suspend fun loadContacts(): List<DeviceContact> = contactReader.readContacts()
}
