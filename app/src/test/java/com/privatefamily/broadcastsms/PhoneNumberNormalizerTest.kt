package com.privatefamily.broadcastsms

import com.privatefamily.broadcastsms.data.contacts.PhoneNumberNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhoneNumberNormalizerTest {
    private val normalizer = PhoneNumberNormalizer()

    @Test
    fun normalize_stripsCommonFormatting() {
        assertEquals("+15551234567", normalizer.normalize("+1 (555) 123-4567"))
    }

    @Test
    fun normalize_rejectsTooShortNumbers() {
        assertNull(normalizer.normalize("555"))
    }
}
