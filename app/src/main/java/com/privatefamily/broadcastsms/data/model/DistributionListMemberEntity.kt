package com.privatefamily.broadcastsms.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "distribution_list_members",
    foreignKeys = [
        ForeignKey(
            entity = DistributionListEntity::class,
            parentColumns = ["id"],
            childColumns = ["distributionListId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("distributionListId"),
        Index("deviceContactId"),
        Index("normalizedPhoneNumber"),
    ],
)
data class DistributionListMemberEntity(
    @PrimaryKey val id: String,
    val distributionListId: String,
    val deviceContactId: String,
    val devicePhoneNumberId: String?,
    val displayName: String,
    val selectedPhoneNumber: String,
    val normalizedPhoneNumber: String?,
    val phoneTypeLabel: String?,
    val firstName: String,
    val lastName: String,
    val relationship: String?,
    val nickname: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
