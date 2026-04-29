package com.privatefamily.broadcastsms.domain.template

private val tokenRegex = "\\{([A-Za-z][A-Za-z0-9]*)}".toRegex()

data class TemplateParseResult(
    val tokens: Set<String>,
    val unsupportedTokens: Set<String>,
)

class TemplateParser(
    private val supportedTokens: Set<String> = SupportedTemplateTokens.all,
) {
    fun parse(template: String): TemplateParseResult {
        val tokens = tokenRegex.findAll(template)
            .map { it.groupValues[1] }
            .toSet()
        return TemplateParseResult(
            tokens = tokens,
            unsupportedTokens = tokens - supportedTokens,
        )
    }
}
