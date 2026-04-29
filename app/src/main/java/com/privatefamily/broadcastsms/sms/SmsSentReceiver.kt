package com.privatefamily.broadcastsms.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val callbackId = intent?.getStringExtra(EXTRA_CALLBACK_ID) ?: return
        SmsSendRegistry.onPartResult(
            callbackId = callbackId,
            resultCode = resultCode,
        )
    }

    companion object {
        const val EXTRA_CALLBACK_ID = "extra_callback_id"
    }
}
