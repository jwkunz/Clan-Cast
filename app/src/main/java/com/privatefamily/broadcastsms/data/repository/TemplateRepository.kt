package com.privatefamily.broadcastsms.data.repository

import com.privatefamily.broadcastsms.data.dao.MessageTemplateDao
import com.privatefamily.broadcastsms.data.model.MessageTemplateEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TemplateRepository(
    private val templateDao: MessageTemplateDao,
) {
    fun observeTemplates(): Flow<List<MessageTemplateEntity>> = templateDao.observeAll()

    suspend fun createTemplate(name: String, body: String) {
        val now = System.currentTimeMillis()
        templateDao.insert(
            MessageTemplateEntity(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                body = body.trim(),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
    }

    suspend fun updateTemplate(template: MessageTemplateEntity, name: String, body: String) {
        templateDao.update(
            template.copy(
                name = name.trim(),
                body = body.trim(),
                updatedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun deleteTemplate(template: MessageTemplateEntity) {
        templateDao.delete(template)
    }

    suspend fun getTemplate(templateId: String): MessageTemplateEntity? = templateDao.getById(templateId)
}
