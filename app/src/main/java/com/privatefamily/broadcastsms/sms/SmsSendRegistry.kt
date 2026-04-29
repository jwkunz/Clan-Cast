package com.privatefamily.broadcastsms.sms

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

object SmsSendRegistry {
    private val callbacks = ConcurrentHashMap<String, PendingSmsSend>()

    fun register(
        callbackId: String,
        expectedParts: Int,
        continuation: Continuation<SmsSendResult>,
    ) {
        callbacks[callbackId] = PendingSmsSend(
            continuation = continuation,
            remainingParts = AtomicInteger(expectedParts),
        )
    }

    fun unregister(callbackId: String) {
        callbacks.remove(callbackId)
    }

    internal fun clearForTest() {
        callbacks.clear()
    }

    fun onPartResult(
        callbackId: String,
        resultCode: Int,
    ) {
        val pending = callbacks[callbackId] ?: return
        if (resultCode != android.app.Activity.RESULT_OK && pending.firstFailure == null) {
            pending.firstFailure = SmsSendResult(
                success = false,
                errorCode = resultCode,
                errorMessage = errorMessageFor(resultCode),
            )
        }

        if (pending.remainingParts.decrementAndGet() == 0 && pending.resumed.compareAndSet(false, true)) {
            callbacks.remove(callbackId)
            pending.continuation.resume(pending.firstFailure ?: SmsSendResult(success = true))
        }
    }

    private fun errorMessageFor(resultCode: Int): String = when (resultCode) {
        android.telephony.SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "Generic SMS failure"
        android.telephony.SmsManager.RESULT_ERROR_NO_SERVICE -> "No carrier service"
        android.telephony.SmsManager.RESULT_ERROR_NULL_PDU -> "Null SMS payload"
        android.telephony.SmsManager.RESULT_ERROR_RADIO_OFF -> "Radio is off"
        else -> "SMS send failed with code $resultCode"
    }

    private class PendingSmsSend(
        val continuation: Continuation<SmsSendResult>,
        val remainingParts: AtomicInteger,
        val resumed: AtomicBoolean = AtomicBoolean(false),
        @Volatile var firstFailure: SmsSendResult? = null,
    )
}
