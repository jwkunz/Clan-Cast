package com.privatefamily.broadcastsms

import com.privatefamily.broadcastsms.data.model.BroadcastEntity
import com.privatefamily.broadcastsms.data.model.BroadcastRecipientEntity
import com.privatefamily.broadcastsms.domain.preflight.BroadcastCreator
import com.privatefamily.broadcastsms.domain.preflight.BroadcastDraft
import com.privatefamily.broadcastsms.domain.preflight.BroadcastSnapshotWriter
import com.privatefamily.broadcastsms.domain.preflight.PreflightRecipient
import com.privatefamily.broadcastsms.domain.preflight.PreflightResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BroadcastCreatorNamingTest {
    @Test
    fun create_usesCustomBroadcastNameWhenProvided() = runBlocking {
        val writer = RecordingSnapshotWriter()
        val creator = BroadcastCreator(writer)

        creator.create(
            draft = baseDraft.copy(broadcastName = "Sunday Dinner Reminder"),
            preflightResult = baseResult,
            duplicatePolicyName = "NONE",
            invalidNumberPolicyName = "NONE",
        )

        assertEquals("Sunday Dinner Reminder", writer.savedBroadcast?.name)
    }

    @Test
    fun create_generatesDefaultNameWhenBlank() = runBlocking {
        val writer = RecordingSnapshotWriter()
        val creator = BroadcastCreator(writer)

        creator.create(
            draft = baseDraft.copy(broadcastName = "   "),
            preflightResult = baseResult,
            duplicatePolicyName = "NONE",
            invalidNumberPolicyName = "NONE",
        )

        assertTrue(writer.savedBroadcast?.name?.startsWith("Broadcast - ") == true)
    }

    private val baseDraft = BroadcastDraft(
        distributionListId = "list-1",
        templateId = "template-1",
        broadcastName = "",
        messageBody = "Hi there",
    )

    private val baseResult = PreflightResult(
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
                renderedMessage = "Hi there",
            ),
        ),
        duplicateIssues = emptyList(),
        invalidNumberIssues = emptyList(),
        missingFieldIssues = emptyList(),
        unsupportedTokenIssues = emptyList(),
        canLaunch = true,
    )
}

private class RecordingSnapshotWriter : BroadcastSnapshotWriter {
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
