package com.privatefamily.broadcastsms

import com.privatefamily.broadcastsms.data.model.DistributionListMemberEntity
import com.privatefamily.broadcastsms.data.model.DuplicatePolicy
import com.privatefamily.broadcastsms.data.model.InvalidNumberPolicy
import com.privatefamily.broadcastsms.domain.preflight.PreflightRequest
import com.privatefamily.broadcastsms.domain.preflight.PreflightValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreflightValidatorTest {
    private val validator = PreflightValidator()

    @Test
    fun validate_reportsMissingFieldsAndUnsupportedTokens() {
        val result = validator.validate(
            request = PreflightRequest(
                distributionListId = "list-1",
                messageBody = "Hi {firstName} {graduationYear}",
            ),
            members = listOf(member(firstName = "", normalizedPhoneNumber = "+15551234567")),
        )

        assertEquals(listOf("graduationYear"), result.unsupportedTokenIssues)
        assertEquals(1, result.missingFieldIssues.size)
        assertFalse(result.canLaunch)
    }

    @Test
    fun validate_appliesPoliciesAndRendersPreview() {
        val result = validator.validate(
            request = PreflightRequest(
                distributionListId = "list-1",
                messageBody = "Hi {firstName}",
                duplicatePolicy = DuplicatePolicy.DEDUPE,
                invalidNumberPolicy = InvalidNumberPolicy.EXCLUDE_INVALID,
            ),
            members = listOf(
                member(id = "1", displayName = "Ava", firstName = "Ava", selectedPhoneNumber = "5551111111", normalizedPhoneNumber = "+15551111111"),
                member(id = "2", displayName = "Ava Duplicate", firstName = "Ava", selectedPhoneNumber = "5551111111", normalizedPhoneNumber = "+15551111111"),
                member(id = "3", displayName = "Bad", firstName = "Bad", selectedPhoneNumber = "555", normalizedPhoneNumber = null),
            ),
        )

        assertTrue(result.canLaunch)
        assertEquals(1, result.duplicateIssues.size)
        assertEquals(1, result.invalidNumberIssues.size)
        assertEquals(1, result.validEntries.size)
        assertEquals("Hi Ava", result.validEntries.single().renderedMessage)
    }

    private fun member(
        id: String = "member-1",
        displayName: String = "Ava Stone",
        firstName: String = "Ava",
        lastName: String = "Stone",
        selectedPhoneNumber: String = "5551234567",
        normalizedPhoneNumber: String? = "+15551234567",
    ) = DistributionListMemberEntity(
        id = id,
        distributionListId = "list-1",
        deviceContactId = "contact-1",
        devicePhoneNumberId = "phone-1",
        displayName = displayName,
        selectedPhoneNumber = selectedPhoneNumber,
        normalizedPhoneNumber = normalizedPhoneNumber,
        phoneTypeLabel = "Mobile",
        firstName = firstName,
        lastName = lastName,
        relationship = null,
        nickname = null,
        createdAtEpochMillis = 0L,
        updatedAtEpochMillis = 0L,
    )
}
