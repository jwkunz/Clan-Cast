package com.privatefamily.broadcastsms

import com.privatefamily.broadcastsms.domain.template.TemplateParser
import org.junit.Assert.assertEquals
import org.junit.Test

class TemplateParserTest {
    private val parser = TemplateParser()

    @Test
    fun parse_extractsUniqueTokens() {
        val result = parser.parse("Hi {firstName}, call {firstName} at {phoneNumber}.")

        assertEquals(setOf("firstName", "phoneNumber"), result.tokens)
    }

    @Test
    fun parse_detectsUnsupportedTokens() {
        val result = parser.parse("Congrats {graduationYear}")

        assertEquals(setOf("graduationYear"), result.unsupportedTokens)
    }
}
