package com.privatefamily.broadcastsms

import com.privatefamily.broadcastsms.domain.template.RenderInput
import com.privatefamily.broadcastsms.domain.template.TemplateRenderer
import org.junit.Assert.assertEquals
import org.junit.Test

class TemplateRendererTest {
    private val renderer = TemplateRenderer()

    @Test
    fun render_replacesSupportedTokens() {
        val result = renderer.render(
            template = "Hi {firstName} ({nickname})",
            input = RenderInput(
                firstName = "Ava",
                lastName = "Stone",
                displayName = "Ava Stone",
                phoneNumber = "5551234567",
                relationship = "Cousin",
                nickname = "Ace",
            ),
        )

        assertEquals("Hi Ava (Ace)", result)
    }
}
