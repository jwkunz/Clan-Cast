package com.privatefamily.broadcastsms.sms

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

class AndroidSmsSender(
    private val context: Context,
    private val smsManager: SmsManager = SmsManager.getDefault(),
) : SmsSender {
    override suspend fun send(recipientId: String, phoneNumber: String, message: String): SmsSendResult {
        return withContext(Dispatchers.Main.immediate) {
            withTimeoutOrNull(SEND_TIMEOUT_MS) {
                suspendCancellableCoroutine { continuation ->
                    val action = "${context.packageName}.SMS_SENT.$recipientId.${System.nanoTime()}"
                    val messageParts = smsManager.divideMessage(message)
                    val remainingParts = AtomicInteger(messageParts.size)
                    val resumed = AtomicBoolean(false)
                    var firstFailure: SmsSendResult? = null

                    val receiver = object : BroadcastReceiver() {
                        override fun onReceive(receiverContext: Context?, intent: Intent?) {
                            if (resultCode != android.app.Activity.RESULT_OK && firstFailure == null) {
                                firstFailure = SmsSendResult(
                                    success = false,
                                    errorCode = resultCode,
                                    errorMessage = errorMessageFor(resultCode),
                                )
                            }

                            if (remainingParts.decrementAndGet() == 0 && resumed.compareAndSet(false, true)) {
                                unregisterQuietly(this)
                                continuation.resume(firstFailure ?: SmsSendResult(success = true))
                            }
                        }
                    }

                    ContextCompat.registerReceiver(
                        context,
                        receiver,
                        IntentFilter(action),
                        ContextCompat.RECEIVER_NOT_EXPORTED,
                    )

                    continuation.invokeOnCancellation {
                        unregisterQuietly(receiver)
                    }

                    if (messageParts.size <= 1) {
                        smsManager.sendTextMessage(
                            phoneNumber,
                            null,
                            message,
                            sentPendingIntent(action, 0),
                            null,
                        )
                    } else {
                        smsManager.sendMultipartTextMessage(
                            phoneNumber,
                            null,
                            messageParts,
                            ArrayList(messageParts.indices.map { sentPendingIntent(action, it) }),
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

    private fun sentPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(action)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun unregisterQuietly(receiver: BroadcastReceiver) {
        runCatching { context.unregisterReceiver(receiver) }
    }

    private fun errorMessageFor(resultCode: Int): String = when (resultCode) {
        SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "Generic SMS failure"
        SmsManager.RESULT_ERROR_NO_SERVICE -> "No carrier service"
        SmsManager.RESULT_ERROR_NULL_PDU -> "Null SMS payload"
        SmsManager.RESULT_ERROR_RADIO_OFF -> "Radio is off"
        else -> "SMS send failed with code $resultCode"
    }

    private companion object {
        const val SEND_TIMEOUT_MS = 30_000L
    }
}
