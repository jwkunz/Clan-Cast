package com.privatefamily.broadcastsms.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_templates")
data class MessageTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val body: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
