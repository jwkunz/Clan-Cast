package com.privatefamily.broadcastsms.sms

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

class AndroidSmsSender(
    private val context: Context,
    private val smsManager: SmsManager = SmsManager.getDefault(),
) : SmsSender {
    override suspend fun send(recipientId: String, phoneNumber: String, message: String): SmsSendResult {
        return withContext(Dispatchers.Main.immediate) {
            withTimeoutOrNull(SEND_TIMEOUT_MS) {
                suspendCancellableCoroutine { continuation ->
                    val callbackId = "$recipientId-${UUID.randomUUID()}"
                    val messageParts = smsManager.divideMessage(message).ifEmpty { arrayListOf(message) }
                    SmsSendRegistry.register(
                        callbackId = callbackId,
                        expectedParts = messageParts.size,
                        continuation = continuation,
                    )

                    continuation.invokeOnCancellation {
                        SmsSendRegistry.unregister(callbackId)
                    }

                    if (messageParts.size <= 1) {
                        smsManager.sendTextMessage(
                            phoneNumber,
                            null,
                            message,
                            sentPendingIntent(callbackId, 0),
                            null,
                        )
                    } else {
                        smsManager.sendMultipartTextMessage(
                            phoneNumber,
                            null,
                            messageParts,
                            ArrayList(messageParts.indices.map { sentPendingIntent(callbackId, it) }),
                            null,
                        )
                    }
                }
            } ?: SmsSendResult(
                success = false,
                errorMessage = "SMS sent callback timeout",
            )
        }
    }

    private fun sentPendingIntent(callbackId: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, SmsSentReceiver::class.java).apply {
            putExtra(SmsSentReceiver.EXTRA_CALLBACK_ID, callbackId)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val SEND_TIMEOUT_MS = 30_000L
    }
}
