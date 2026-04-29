package com.privatefamily.broadcastsms.data.repository

import com.privatefamily.broadcastsms.data.dao.BroadcastDao
import com.privatefamily.broadcastsms.data.dao.BroadcastRecipientDao
import com.privatefamily.broadcastsms.data.AppDatabase
import com.privatefamily.broadcastsms.data.model.BroadcastEntity
import com.privatefamily.broadcastsms.data.model.BroadcastRecipientEntity
import com.privatefamily.broadcastsms.data.model.BroadcastStatus
import com.privatefamily.broadcastsms.data.model.RecipientStatus
import androidx.room.withTransaction
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow
import com.privatefamily.broadcastsms.domain.preflight.BroadcastSnapshotWriter
import com.privatefamily.broadcastsms.permissions.AppPermission

open class BroadcastRepository(
    private val database: AppDatabase,
    private val broadcastDao: BroadcastDao,
    private val recipientDao: BroadcastRecipientDao,
) : BroadcastSnapshotWriter {
    fun observeBroadcasts(): Flow<List<BroadcastEntity>> = broadcastDao.observeAll()

    fun observeBroadcast(broadcastId: String): Flow<BroadcastEntity?> = broadcastDao.observeById(broadcastId)

    fun observeRecipients(broadcastId: String): Flow<List<BroadcastRecipientEntity>> =
        recipientDao.observeByBroadcastId(broadcastId)

    suspend fun getBroadcast(broadcastId: String): BroadcastEntity? = broadcastDao.getById(broadcastId)

    suspend fun getRecipients(broadcastId: String): List<BroadcastRecipientEntity> = recipientDao.getByBroadcastId(broadcastId)

    suspend fun getNextPendingRecipient(broadcastId: String): BroadcastRecipientEntity? =
        recipientDao.getPendingByBroadcastId(broadcastId).firstOrNull()

    suspend fun insertBroadcast(broadcast: BroadcastEntity) = broadcastDao.insert(broadcast)

    suspend fun insertRecipients(recipients: List<BroadcastRecipientEntity>) = recipientDao.insertAll(recipients)

    suspend fun deleteBroadcast(broadcastId: String) {
        broadcastDao.deleteById(broadcastId)
    }

    suspend fun deleteAllBroadcasts() {
        broadcastDao.deleteAll()
    }

    override suspend fun createBroadcastSnapshot(
        broadcast: BroadcastEntity,
        recipients: List<BroadcastRecipientEntity>,
    ) {
        database.withTransaction {
            broadcastDao.insert(broadcast)
            recipientDao.insertAll(recipients)
        }
    }

    suspend fun updateStatus(broadcastId: String, status: BroadcastStatus) {
        broadcastDao.updateStatus(broadcastId, status, System.currentTimeMillis())
    }

    suspend fun updateRecipientStatus(
        recipientId: String,
        status: RecipientStatus,
        errorCode: Int? = null,
        errorMessage: String? = null,
    ) {
        recipientDao.updateRecipientStatus(
            recipientId = recipientId,
            status = status,
            errorCode = errorCode,
            errorMessage = errorMessage,
            updatedAt = System.currentTimeMillis(),
        )
    }

    suspend fun markPendingRecipientsCancelled(broadcastId: String) {
        recipientDao.markPendingAsCancelled(broadcastId, System.currentTimeMillis())
    }

    suspend fun refreshCounts(broadcastId: String) {
        val recipients = recipientDao.getByBroadcastId(broadcastId)
        broadcastDao.updateCounts(
            broadcastId = broadcastId,
            totalRecipients = recipients.size,
            sentCount = recipients.count { it.status == RecipientStatus.SENT },
            failedCount = recipients.count { it.status == RecipientStatus.FAILED },
            cancelledCount = recipients.count { it.status == RecipientStatus.CANCELLED },
            updatedAt = System.currentTimeMillis(),
        )
    }

    fun hasSmsPermission(context: android.content.Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            AppPermission.SendSms.manifestValue,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    suspend fun recoverInterruptedBroadcasts() {
        broadcastDao.pauseAllRunning(System.currentTimeMillis())
    }
}
