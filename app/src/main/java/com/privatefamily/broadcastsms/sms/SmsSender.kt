package com.privatefamily.broadcastsms.sms

interface SmsSender {
    suspend fun send(recipientId: String, phoneNumber: String, message: String): SmsSendResult
}

data class SmsSendResult(
    val success: Boolean,
    val errorCode: Int? = null,
    val errorMessage: String? = null,
)
