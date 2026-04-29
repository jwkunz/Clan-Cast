package com.privatefamily.broadcastsms.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "broadcast_recipients",
    foreignKeys = [
        ForeignKey(
            entity = BroadcastEntity::class,
            parentColumns = ["id"],
            childColumns = ["broadcastId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("broadcastId"), Index("status")],
)
data class BroadcastRecipientEntity(
    @PrimaryKey val id: String,
    val broadcastId: String,
    val distributionListMemberId: String?,
    val displayName: String,
    val firstName: String?,
    val lastName: String?,
    val relationship: String?,
    val nickname: String?,
    val phoneNumber: String,
    val normalizedPhoneNumber: String?,
    val renderedMessage: String,
    val status: RecipientStatus,
    val errorCode: Int?,
    val errorMessage: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
