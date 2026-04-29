package com.privatefamily.broadcastsms

import android.app.Activity
import android.telephony.SmsManager
import com.privatefamily.broadcastsms.sms.SmsSendRegistry
import com.privatefamily.broadcastsms.sms.SmsSendResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class SmsSendRegistryTest {
    @Test
    fun multipartSuccess_resumesAfterAllParts() = runBlocking {
        SmsSendRegistry.clearForTest()
        val deferred = registerDeferred("callback-1", expectedParts = 2)

        SmsSendRegistry.onPartResult("callback-1", Activity.RESULT_OK)
        assertFalse(deferred.isCompleted)

        SmsSendRegistry.onPartResult("callback-1", Activity.RESULT_OK)
        val result = deferred.await()

        assertTrue(result.success)
        assertNull(result.errorCode)
    }

    @Test
    fun multipartFailure_keepsFirstFailure() = runBlocking {
        SmsSendRegistry.clearForTest()
        val deferred = registerDeferred("callback-2", expectedParts = 3)

        SmsSendRegistry.onPartResult("callback-2", SmsManager.RESULT_ERROR_NO_SERVICE)
        SmsSendRegistry.onPartResult("callback-2", Activity.RESULT_OK)
        SmsSendRegistry.onPartResult("callback-2", SmsManager.RESULT_ERROR_RADIO_OFF)
        val result = deferred.await()

        assertFalse(result.success)
        assertEquals(SmsManager.RESULT_ERROR_NO_SERVICE, result.errorCode)
        assertEquals("No carrier service", result.errorMessage)
    }

    @Test
    fun unregister_dropsPendingCallback() = runBlocking {
        SmsSendRegistry.clearForTest()
        val deferred = registerDeferred("callback-3", expectedParts = 1)

        SmsSendRegistry.unregister("callback-3")
        SmsSendRegistry.onPartResult("callback-3", Activity.RESULT_OK)

        val result = withTimeoutOrNull(100) { deferred.await() }
        assertNull(result)
        deferred.cancel()
    }

    private fun registerDeferred(
        callbackId: String,
        expectedParts: Int,
    ): CompletableDeferred<SmsSendResult> {
        val deferred = CompletableDeferred<SmsSendResult>()
        SmsSendRegistry.register(
            callbackId = callbackId,
            expectedParts = expectedParts,
            continuation = object : Continuation<SmsSendResult> {
                override val context: CoroutineContext = EmptyCoroutineContext

                override fun resumeWith(result: Result<SmsSendResult>) {
                    result.onSuccess { deferred.complete(it) }
                    result.onFailure { deferred.completeExceptionally(it) }
                }
            },
        )
        return deferred
    }
}
