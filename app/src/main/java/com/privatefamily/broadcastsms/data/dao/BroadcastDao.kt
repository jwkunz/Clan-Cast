package com.privatefamily.broadcastsms.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.privatefamily.broadcastsms.data.model.BroadcastEntity
import com.privatefamily.broadcastsms.data.model.BroadcastStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BroadcastDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(broadcast: BroadcastEntity)

    @Update
    suspend fun update(broadcast: BroadcastEntity)

    @Query("SELECT * FROM broadcasts ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<BroadcastEntity>>

    @Query("SELECT * FROM broadcasts WHERE id = :broadcastId LIMIT 1")
    fun observeById(broadcastId: String): Flow<BroadcastEntity?>

    @Query("SELECT * FROM broadcasts WHERE id = :broadcastId LIMIT 1")
    suspend fun getById(broadcastId: String): BroadcastEntity?

    @Query("UPDATE broadcasts SET status = :status, updatedAtEpochMillis = :updatedAt WHERE id = :broadcastId")
    suspend fun updateStatus(broadcastId: String, status: BroadcastStatus, updatedAt: Long)

    @Query(
        """
        UPDATE broadcasts
        SET totalRecipients = :totalRecipients,
            sentCount = :sentCount,
            failedCount = :failedCount,
            cancelledCount = :cancelledCount,
            updatedAtEpochMillis = :updatedAt
        WHERE id = :broadcastId
        """,
    )
    suspend fun updateCounts(
        broadcastId: String,
        totalRecipients: Int,
        sentCount: Int,
        failedCount: Int,
        cancelledCount: Int,
        updatedAt: Long,
    )
}
