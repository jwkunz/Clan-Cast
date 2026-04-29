package com.privatefamily.broadcastsms

import com.privatefamily.broadcastsms.data.model.BroadcastEntity
import com.privatefamily.broadcastsms.data.model.BroadcastRecipientEntity
import com.privatefamily.broadcastsms.domain.preflight.BroadcastCreator
import com.privatefamily.broadcastsms.domain.preflight.BroadcastDraft
import com.privatefamily.broadcastsms.domain.preflight.PreflightRecipient
import com.privatefamily.broadcastsms.domain.preflight.PreflightResult
import com.privatefamily.broadcastsms.domain.preflight.BroadcastSnapshotWriter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BroadcastCreatorTest {
    @Test
    fun create_persistsBroadcastAndRecipients() = runBlocking {
        val repository = RecordingBroadcastRepository()
        val creator = BroadcastCreator(repository)

        val broadcastId = creator.create(
            draft = BroadcastDraft(
                distributionListId = "list-1",
                templateId = "template-1",
                messageBody = "Hi {firstName}",
            ),
            preflightResult = PreflightResult(
                totalEntries = 1,
                uniquePhoneNumberCount = 1,
                validEntries = listOf(
                    PreflightRecipient(
                        distributionListMemberId = "member-1",
                        displayName = "Ava",
                        firstName = "Ava",
                        lastName = "Stone",
                        relationship = null,
                        nickname = null,
                        phoneNumber = "5551234567",
                        normalizedPhoneNumber = "+15551234567",
                        renderedMessage = "Hi Ava",
                    ),
                ),
                duplicateIssues = emptyList(),
                invalidNumberIssues = emptyList(),
                missingFieldIssues = emptyList(),
                unsupportedTokenIssues = emptyList(),
                canLaunch = true,
            ),
            duplicatePolicyName = "DEDUPE",
            invalidNumberPolicyName = "EXCLUDE_INVALID",
        )

        assertEquals(repository.savedBroadcast?.id, broadcastId)
        assertEquals(1, repository.savedRecipients.size)
        assertTrue(repository.savedBroadcast?.name?.startsWith("Broadcast - ") == true)
    }
}

private class RecordingBroadcastRepository : BroadcastSnapshotWriter {
    var savedBroadcast: BroadcastEntity? = null
    var savedRecipients: List<BroadcastRecipientEntity> = emptyList()

    override suspend fun createBroadcastSnapshot(
        broadcast: BroadcastEntity,
        recipients: List<BroadcastRecipientEntity>,
    ) {
        savedBroadcast = broadcast
        savedRecipients = recipients
    }
}
