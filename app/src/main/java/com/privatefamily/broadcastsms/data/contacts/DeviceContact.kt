package com.privatefamily.broadcastsms.data.contacts

data class DeviceContact(
    val contactId: String,
    val displayName: String,
    val firstName: String,
    val lastName: String,
    val phoneNumbers: List<DevicePhoneNumber>,
)

data class DevicePhoneNumber(
    val phoneNumberId: String?,
    val number: String,
    val normalizedNumber: String?,
    val typeLabel: String?,
)
