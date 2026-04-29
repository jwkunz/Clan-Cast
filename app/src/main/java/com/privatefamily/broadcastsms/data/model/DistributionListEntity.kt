package com.privatefamily.broadcastsms.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "distribution_lists")
data class DistributionListEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
