package com.privatefamily.broadcastsms.domain.preflight

import com.privatefamily.broadcastsms.data.model.DistributionListMemberEntity
import com.privatefamily.broadcastsms.data.model.DuplicatePolicy
import com.privatefamily.broadcastsms.data.model.InvalidNumberPolicy
import com.privatefamily.broadcastsms.domain.template.RenderInput
import com.privatefamily.broadcastsms.domain.template.SupportedTemplateTokens
import com.privatefamily.broadcastsms.domain.template.TemplateParser
import com.privatefamily.broadcastsms.domain.template.TemplateRenderer

class PreflightValidator(
    private val templateParser: TemplateParser = TemplateParser(),
    private val templateRenderer: TemplateRenderer = TemplateRenderer(),
) {
    fun validate(
        request: PreflightRequest,
        members: List<DistributionListMemberEntity>,
    ): PreflightResult {
        val parseResult = templateParser.parse(request.messageBody)
        val missingFieldIssues = buildMissingFieldIssues(
            members = members,
            messageBody = request.messageBody,
            usedTokens = parseResult.tokens,
        )
        val duplicateIssues = buildDuplicateIssues(members)
        val invalidNumberIssues = buildInvalidNumberIssues(members)
        val validEntries = if (
            parseResult.unsupportedTokens.isEmpty() &&
            missingFieldIssues.isEmpty() &&
            policiesSatisfied(
                duplicateIssues = duplicateIssues,
                invalidNumberIssues = invalidNumberIssues,
                request = request,
            )
        ) {
            buildRenderedEntries(
                members = members,
                messageBody = request.messageBody,
                duplicatePolicy = request.duplicatePolicy,
                invalidNumberPolicy = request.invalidNumberPolicy,
            )
        } else {
            emptyList()
        }

        return PreflightResult(
            totalEntries = members.size,
            uniquePhoneNumberCount = members.mapNotNull { it.normalizedPhoneNumber }.toSet().size,
            validEntries = validEntries,
            duplicateIssues = duplicateIssues,
            invalidNumberIssues = invalidNumberIssues,
            missingFieldIssues = missingFieldIssues,
            unsupportedTokenIssues = parseResult.unsupportedTokens.toList().sorted(),
            canLaunch = parseResult.unsupportedTokens.isEmpty() &&
                missingFieldIssues.isEmpty() &&
                policiesSatisfied(
                    duplicateIssues = duplicateIssues,
                    invalidNumberIssues = invalidNumberIssues,
                    request = request,
                ),
        )
    }

    private fun buildMissingFieldIssues(
        members: List<DistributionListMemberEntity>,
        messageBody: String,
        usedTokens: Set<String>,
    ): List<MissingFieldIssue> {
        if (messageBody.isBlank() || usedTokens.isEmpty()) return emptyList()
        val supportedUsedTokens = usedTokens.intersect(SupportedTemplateTokens.all)

        return buildList {
            members.forEach { member ->
                supportedUsedTokens.forEach { token ->
                    if (member.tokenValue(token).isNullOrBlank()) {
                        add(
                            MissingFieldIssue(
                                displayName = member.displayName,
                                fieldName = token,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun buildDuplicateIssues(
        members: List<DistributionListMemberEntity>,
    ): List<DuplicateIssue> {
        return members
            .filter { !it.normalizedPhoneNumber.isNullOrBlank() }
            .groupBy { it.normalizedPhoneNumber.orEmpty() }
            .filterValues { it.size > 1 }
            .map { (normalizedPhoneNumber, groupedMembers) ->
                DuplicateIssue(
                    normalizedPhoneNumber = normalizedPhoneNumber,
                    displayNames = groupedMembers.map { "${it.displayName} (${it.selectedPhoneNumber})" },
                )
            }
            .sortedBy { it.normalizedPhoneNumber }
    }

    private fun buildInvalidNumberIssues(
        members: List<DistributionListMemberEntity>,
    ): List<InvalidNumberIssue> {
        return members
            .filter { it.normalizedPhoneNumber.isNullOrBlank() }
            .map {
                InvalidNumberIssue(
                    displayName = it.displayName,
                    phoneNumber = it.selectedPhoneNumber,
                    reason = "Unable to normalize number",
                )
            }
    }

    private fun buildRenderedEntries(
        members: List<DistributionListMemberEntity>,
        messageBody: String,
        duplicatePolicy: DuplicatePolicy?,
        invalidNumberPolicy: InvalidNumberPolicy?,
    ): List<PreflightRecipient> {
        val filteredInvalids = when (invalidNumberPolicy) {
            InvalidNumberPolicy.EXCLUDE_INVALID -> members.filter { !it.normalizedPhoneNumber.isNullOrBlank() }
            else -> members
        }

        val dedupedMembers = when (duplicatePolicy) {
            DuplicatePolicy.DEDUPE -> filteredInvalids.distinctBy { it.normalizedPhoneNumber ?: it.id }
            else -> filteredInvalids
        }

        return dedupedMembers.map { member ->
            PreflightRecipient(
                distributionListMemberId = member.id,
                displayName = member.displayName,
                firstName = member.firstName,
                lastName = member.lastName,
                relationship = member.relationship,
                nickname = member.nickname,
                phoneNumber = member.selectedPhoneNumber,
                normalizedPhoneNumber = member.normalizedPhoneNumber,
                renderedMessage = templateRenderer.render(
                    template = messageBody,
                    input = member.asRenderInput(),
                ),
            )
        }
    }

    private fun policiesSatisfied(
        duplicateIssues: List<DuplicateIssue>,
        invalidNumberIssues: List<InvalidNumberIssue>,
        request: PreflightRequest,
    ): Boolean {
        val duplicateSatisfied = duplicateIssues.isEmpty() || request.duplicatePolicy != null
        val invalidSatisfied = invalidNumberIssues.isEmpty() || request.invalidNumberPolicy != null
        return duplicateSatisfied && invalidSatisfied
    }
}

private fun DistributionListMemberEntity.asRenderInput(): RenderInput = RenderInput(
    firstName = firstName,
    lastName = lastName,
    displayName = displayName,
    phoneNumber = selectedPhoneNumber,
    relationship = relationship,
    nickname = nickname,
)

private fun DistributionListMemberEntity.tokenValue(token: String): String? = when (token) {
    "firstName" -> firstName
    "lastName" -> lastName
    "displayName" -> displayName
    "phoneNumber" -> selectedPhoneNumber
    "relationship" -> relationship
    "nickname" -> nickname
    else -> null
}
