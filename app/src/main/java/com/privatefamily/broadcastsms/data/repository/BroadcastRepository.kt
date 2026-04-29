package com.privatefamily.broadcastsms.data.repository

import com.privatefamily.broadcastsms.data.dao.BroadcastDao
import com.privatefamily.broadcastsms.data.dao.BroadcastRecipientDao
import com.privatefamily.broadcastsms.data.model.BroadcastEntity
import com.privatefamily.broadcastsms.data.model.BroadcastRecipientEntity
import com.privatefamily.broadcastsms.data.model.BroadcastStatus
import com.privatefamily.broadcastsms.data.model.RecipientStatus
import kotlinx.coroutines.flow.Flow

class BroadcastRepository(
    private val broadcastDao: BroadcastDao,
    private val recipientDao: BroadcastRecipientDao,
) {
    fun observeBroadcasts(): Flow<List<BroadcastEntity>> = broadcastDao.observeAll()

    fun observeBroadcast(broadcastId: String): Flow<BroadcastEntity?> = broadcastDao.observeById(broadcastId)

    fun observeRecipients(broadcastId: String): Flow<List<BroadcastRecipientEntity>> =
        recipientDao.observeByBroadcastId(broadcastId)

    suspend fun insertBroadcast(broadcast: BroadcastEntity) = broadcastDao.insert(broadcast)

    suspend fun insertRecipients(recipients: List<BroadcastRecipientEntity>) = recipientDao.insertAll(recipients)

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
}
