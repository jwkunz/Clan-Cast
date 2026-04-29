package com.privatefamily.broadcastsms.domain.preflight

import com.privatefamily.broadcastsms.data.model.BroadcastEntity
import com.privatefamily.broadcastsms.data.model.BroadcastRecipientEntity
import com.privatefamily.broadcastsms.data.model.BroadcastStatus
import com.privatefamily.broadcastsms.data.model.RecipientStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BroadcastCreator(
    private val snapshotWriter: BroadcastSnapshotWriter,
) {
    suspend fun create(
        draft: BroadcastDraft,
        preflightResult: PreflightResult,
        duplicatePolicyName: String,
        invalidNumberPolicyName: String,
    ): String {
        val broadcastId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val duplicatePolicy = enumValueOf<com.privatefamily.broadcastsms.data.model.DuplicatePolicy>(duplicatePolicyName)
        val invalidNumberPolicy = enumValueOf<com.privatefamily.broadcastsms.data.model.InvalidNumberPolicy>(invalidNumberPolicyName)
        val broadcast = BroadcastEntity(
            id = broadcastId,
            name = "Broadcast - ${timestampLabel(now)}",
            distributionListId = draft.distributionListId.orEmpty(),
            sourceTemplateId = draft.templateId,
            messageBody = draft.messageBody,
            status = BroadcastStatus.PENDING,
            duplicatePolicy = duplicatePolicy,
            invalidNumberPolicy = invalidNumberPolicy,
            totalRecipients = preflightResult.validEntries.size,
            sentCount = 0,
            failedCount = 0,
            cancelledCount = 0,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now,
        )
        val recipients = preflightResult.validEntries.map { preview ->
            BroadcastRecipientEntity(
                id = UUID.randomUUID().toString(),
                broadcastId = broadcastId,
                distributionListMemberId = preview.distributionListMemberId,
                displayName = preview.displayName,
                firstName = preview.firstName,
                lastName = preview.lastName,
                relationship = preview.relationship,
                nickname = preview.nickname,
                phoneNumber = preview.phoneNumber,
                normalizedPhoneNumber = preview.normalizedPhoneNumber,
                renderedMessage = preview.renderedMessage,
                status = RecipientStatus.PENDING,
                errorCode = null,
                errorMessage = null,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            )
        }

        snapshotWriter.createBroadcastSnapshot(broadcast, recipients)
        return broadcastId
    }

    private fun timestampLabel(epochMillis: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        return formatter.format(Date(epochMillis))
    }
}
