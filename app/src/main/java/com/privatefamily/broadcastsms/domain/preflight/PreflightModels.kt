package com.privatefamily.broadcastsms.domain.preflight

import com.privatefamily.broadcastsms.data.model.DuplicatePolicy
import com.privatefamily.broadcastsms.data.model.InvalidNumberPolicy

data class BroadcastDraft(
    val distributionListId: String? = null,
    val templateId: String? = null,
    val messageBody: String = "",
)

data class PreflightRequest(
    val distributionListId: String,
    val messageBody: String,
    val duplicatePolicy: DuplicatePolicy? = null,
    val invalidNumberPolicy: InvalidNumberPolicy? = null,
)

data class PreflightRecipient(
    val distributionListMemberId: String,
    val displayName: String,
    val firstName: String?,
    val lastName: String?,
    val relationship: String?,
    val nickname: String?,
    val phoneNumber: String,
    val normalizedPhoneNumber: String?,
    val renderedMessage: String,
)

data class DuplicateIssue(
    val normalizedPhoneNumber: String,
    val displayNames: List<String>,
)

data class InvalidNumberIssue(
    val displayName: String,
    val phoneNumber: String,
    val reason: String,
)

data class MissingFieldIssue(
    val displayName: String,
    val fieldName: String,
)

data class PreflightResult(
    val totalEntries: Int,
    val uniquePhoneNumberCount: Int,
    val validEntries: List<PreflightRecipient>,
    val duplicateIssues: List<DuplicateIssue>,
    val invalidNumberIssues: List<InvalidNumberIssue>,
    val missingFieldIssues: List<MissingFieldIssue>,
    val unsupportedTokenIssues: List<String>,
    val canLaunch: Boolean,
)
