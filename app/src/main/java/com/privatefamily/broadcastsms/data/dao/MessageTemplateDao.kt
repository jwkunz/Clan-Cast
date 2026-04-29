package com.privatefamily.broadcastsms.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.privatefamily.broadcastsms.data.model.MessageTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageTemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: MessageTemplateEntity)

    @Update
    suspend fun update(template: MessageTemplateEntity)

    @Delete
    suspend fun delete(template: MessageTemplateEntity)

    @Query("SELECT * FROM message_templates ORDER BY updatedAtEpochMillis DESC")
    fun observeAll(): Flow<List<MessageTemplateEntity>>

    @Query("SELECT * FROM message_templates WHERE id = :templateId LIMIT 1")
    suspend fun getById(templateId: String): MessageTemplateEntity?
}
