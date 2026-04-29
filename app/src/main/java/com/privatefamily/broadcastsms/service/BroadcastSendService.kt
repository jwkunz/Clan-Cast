package com.privatefamily.broadcastsms.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.privatefamily.broadcastsms.ClanCastApp
import com.privatefamily.broadcastsms.R
import com.privatefamily.broadcastsms.data.model.BroadcastStatus
import com.privatefamily.broadcastsms.data.model.RecipientStatus
import com.privatefamily.broadcastsms.permissions.AppPermission
import com.privatefamily.broadcastsms.sms.AndroidSmsSender
import com.privatefamily.broadcastsms.sms.SmsSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BroadcastSendService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repository by lazy { (application as ClanCastApp).container.broadcastRepository }
    private lateinit var smsSender: SmsSender
    private var activeBroadcastId: String? = null
    private var sendLoopJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        smsSender = AndroidSmsSender(this)
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val broadcastId = intent?.getStringExtra(EXTRA_BROADCAST_ID)
        when (intent?.action) {
            ACTION_START_BROADCAST -> {
                if (broadcastId != null) {
                    activeBroadcastId = broadcastId
                    startForeground(NOTIFICATION_ID, buildNotification("Starting broadcast"))
                    launchSendLoopIfNeeded(broadcastId) {
                        repository.updateStatus(broadcastId, BroadcastStatus.RUNNING)
                        runBroadcastLoop(broadcastId)
                    }
                }
            }

            ACTION_PAUSE_BROADCAST -> {
                if (broadcastId != null) {
                    serviceScope.launch {
                        repository.updateStatus(broadcastId, BroadcastStatus.PAUSED)
                        updateNotification("Paused")
                    }
                }
            }

            ACTION_RESUME_BROADCAST -> {
                if (broadcastId != null) {
                    activeBroadcastId = broadcastId
                    startForeground(NOTIFICATION_ID, buildNotification("Resuming broadcast"))
                    launchSendLoopIfNeeded(broadcastId) {
                        repository.updateStatus(broadcastId, BroadcastStatus.RUNNING)
                        runBroadcastLoop(broadcastId)
                    }
                }
            }

            ACTION_CANCEL_BROADCAST -> {
                if (broadcastId != null) {
                    serviceScope.launch {
                        repository.updateStatus(broadcastId, BroadcastStatus.CANCELLED)
                    }
                }
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        sendLoopJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun launchSendLoopIfNeeded(
        broadcastId: String,
        block: suspend () -> Unit,
    ) {
        if (activeBroadcastId == broadcastId && sendLoopJob?.isActive == true) {
            return
        }
        sendLoopJob?.cancel()
        sendLoopJob = serviceScope.launch {
            try {
                block()
            } finally {
                if (activeBroadcastId == broadcastId) {
                    sendLoopJob = null
                }
            }
        }
    }

    private suspend fun runBroadcastLoop(broadcastId: String) {
        if (!repository.hasSmsPermission(this)) {
            repository.updateStatus(broadcastId, BroadcastStatus.FAILED)
            repository.refreshCounts(broadcastId)
            updateNotification("SMS permission missing")
            stopSelf()
            return
        }

        while (serviceScope.isActive && activeBroadcastId == broadcastId) {
            val broadcast = repository.getBroadcast(broadcastId) ?: break
            when (broadcast.status) {
                BroadcastStatus.PAUSED -> {
                    updateNotification(progressText(broadcast.sentCount, broadcast.totalRecipients, "Paused"))
                    delay(POLL_DELAY_MS)
                    continue
                }

                BroadcastStatus.CANCELLED -> {
                    repository.markPendingRecipientsCancelled(broadcastId)
                    repository.refreshCounts(broadcastId)
                    updateNotification(finalText("Cancelled", broadcastId))
                    activeBroadcastId = null
                    stopSelf()
                    return
                }

                BroadcastStatus.RUNNING -> Unit
                BroadcastStatus.PENDING -> repository.updateStatus(broadcastId, BroadcastStatus.RUNNING)
                BroadcastStatus.COMPLETED, BroadcastStatus.FAILED -> {
                    activeBroadcastId = null
                    stopSelf()
                    return
                }
            }

            val nextRecipient = repository.getNextPendingRecipient(broadcastId)
            if (nextRecipient == null) {
                repository.refreshCounts(broadcastId)
                repository.updateStatus(broadcastId, BroadcastStatus.COMPLETED)
                updateNotification(finalText("Completed", broadcastId))
                activeBroadcastId = null
                stopSelf()
                return
            }

            updateNotification(
                progressText(
                    processedCount = broadcast.sentCount + broadcast.failedCount + broadcast.cancelledCount,
                    totalCount = broadcast.totalRecipients,
                    prefix = "Running",
                ),
            )

            val sendResult = smsSender.send(
                recipientId = nextRecipient.id,
                phoneNumber = nextRecipient.phoneNumber,
                message = nextRecipient.renderedMessage,
            )
            repository.updateRecipientStatus(
                recipientId = nextRecipient.id,
                status = if (sendResult.success) RecipientStatus.SENT else RecipientStatus.FAILED,
                errorCode = sendResult.errorCode,
                errorMessage = sendResult.errorMessage,
            )
            repository.refreshCounts(broadcastId)
            delay(SEND_DELAY_MS)
        }
    }

    private fun updateNotification(message: String) {
        val manager = getSystemService<NotificationManager>() ?: return
        manager.notify(NOTIFICATION_ID, buildNotification(message))
    }

    private fun buildNotification(message: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Sending Broadcast")
            .setContentText(message)
            .setOngoing(true)
            .build()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService<NotificationManager>() ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Broadcast Sending",
            NotificationManager.IMPORTANCE_LOW,
        )
        manager.createNotificationChannel(channel)
    }

    private fun progressText(processedCount: Int, totalCount: Int, prefix: String): String {
        return "$prefix: $processedCount of $totalCount processed"
    }

    private suspend fun finalText(prefix: String, broadcastId: String): String {
        val recipients = repository.getRecipients(broadcastId)
        val sent = recipients.count { it.status == RecipientStatus.SENT }
        val failed = recipients.count { it.status == RecipientStatus.FAILED }
        val cancelled = recipients.count { it.status == RecipientStatus.CANCELLED }
        return "$prefix: $sent sent, $failed failed, $cancelled cancelled"
    }

    companion object {
        private const val CHANNEL_ID = "broadcast_sending"
        private const val NOTIFICATION_ID = 2201
        private const val SEND_DELAY_MS = 3_000L
        private const val POLL_DELAY_MS = 500L

        const val ACTION_START_BROADCAST = "com.privatefamily.broadcastsms.action.START_BROADCAST"
        const val ACTION_PAUSE_BROADCAST = "com.privatefamily.broadcastsms.action.PAUSE_BROADCAST"
        const val ACTION_RESUME_BROADCAST = "com.privatefamily.broadcastsms.action.RESUME_BROADCAST"
        const val ACTION_CANCEL_BROADCAST = "com.privatefamily.broadcastsms.action.CANCEL_BROADCAST"
        const val EXTRA_BROADCAST_ID = "extra_broadcast_id"

        fun start(context: Context, broadcastId: String) {
            ContextCompat.startForegroundService(
                context,
                intent(context, ACTION_START_BROADCAST, broadcastId),
            )
        }

        fun pause(context: Context, broadcastId: String) {
            context.startService(intent(context, ACTION_PAUSE_BROADCAST, broadcastId))
        }

        fun resume(context: Context, broadcastId: String) {
            ContextCompat.startForegroundService(
                context,
                intent(context, ACTION_RESUME_BROADCAST, broadcastId),
            )
        }

        fun cancel(context: Context, broadcastId: String) {
            context.startService(intent(context, ACTION_CANCEL_BROADCAST, broadcastId))
        }

        private fun intent(context: Context, action: String, broadcastId: String): Intent {
            return Intent(context, BroadcastSendService::class.java).apply {
                this.action = action
                putExtra(EXTRA_BROADCAST_ID, broadcastId)
            }
        }
    }
}
