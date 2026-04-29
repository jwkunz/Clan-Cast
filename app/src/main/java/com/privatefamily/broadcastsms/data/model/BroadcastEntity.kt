package com.privatefamily.broadcastsms.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "broadcasts")
data class BroadcastEntity(
    @PrimaryKey val id: String,
    val name: String,
    val distributionListId: String,
    val sourceTemplateId: String?,
    val messageBody: String,
    val status: BroadcastStatus,
    val duplicatePolicy: DuplicatePolicy,
    val invalidNumberPolicy: InvalidNumberPolicy,
    val totalRecipients: Int,
    val sentCount: Int,
    val failedCount: Int,
    val cancelledCount: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
