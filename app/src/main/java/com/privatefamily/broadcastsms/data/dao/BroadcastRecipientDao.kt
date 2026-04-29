package com.privatefamily.broadcastsms.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.privatefamily.broadcastsms.data.model.BroadcastRecipientEntity
import com.privatefamily.broadcastsms.data.model.RecipientStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BroadcastRecipientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipients: List<BroadcastRecipientEntity>)

    @Query("SELECT * FROM broadcast_recipients WHERE broadcastId = :broadcastId ORDER BY createdAtEpochMillis ASC")
    fun observeByBroadcastId(broadcastId: String): Flow<List<BroadcastRecipientEntity>>

    @Query("SELECT * FROM broadcast_recipients WHERE broadcastId = :broadcastId AND status = 'PENDING' ORDER BY createdAtEpochMillis ASC")
    suspend fun getPendingByBroadcastId(broadcastId: String): List<BroadcastRecipientEntity>

    @Query(
        """
        UPDATE broadcast_recipients
        SET status = :status,
            errorCode = :errorCode,
            errorMessage = :errorMessage,
            updatedAtEpochMillis = :updatedAt
        WHERE id = :recipientId
        """,
    )
    suspend fun updateRecipientStatus(
        recipientId: String,
        status: RecipientStatus,
        errorCode: Int?,
        errorMessage: String?,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE broadcast_recipients
        SET status = 'CANCELLED',
            updatedAtEpochMillis = :updatedAt
        WHERE broadcastId = :broadcastId AND status = 'PENDING'
        """,
    )
    suspend fun markPendingAsCancelled(broadcastId: String, updatedAt: Long)
}
