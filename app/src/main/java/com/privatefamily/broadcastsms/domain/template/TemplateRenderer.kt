package com.privatefamily.broadcastsms.domain.template

data class RenderInput(
    val firstName: String?,
    val lastName: String?,
    val displayName: String,
    val phoneNumber: String,
    val relationship: String?,
    val nickname: String?,
)

object SupportedTemplateTokens {
    val all: Set<String> = setOf(
        "firstName",
        "lastName",
        "displayName",
        "phoneNumber",
        "relationship",
        "nickname",
    )
}

class TemplateRenderer {
    fun render(template: String, input: RenderInput): String {
        return SupportedTemplateTokens.all.fold(template) { current, token ->
            current.replace("{$token}", input.valueFor(token).orEmpty())
        }
    }

    private fun RenderInput.valueFor(token: String): String? = when (token) {
        "firstName" -> firstName
        "lastName" -> lastName
        "displayName" -> displayName
        "phoneNumber" -> phoneNumber
        "relationship" -> relationship
        "nickname" -> nickname
        else -> null
    }
}
