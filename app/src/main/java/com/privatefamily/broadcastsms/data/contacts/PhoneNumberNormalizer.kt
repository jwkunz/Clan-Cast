package com.privatefamily.broadcastsms.data.contacts

class PhoneNumberNormalizer {
    fun normalize(number: String): String? {
        val trimmed = number.trim()
        if (trimmed.isBlank()) return null

        val builder = StringBuilder()
        trimmed.forEachIndexed { index, char ->
            when {
                char.isDigit() -> builder.append(char)
                char == '+' && index == 0 -> builder.append(char)
            }
        }

        val normalized = builder.toString()
        val digitsOnly = normalized.filter { it.isDigit() }
        return normalized.takeIf { digitsOnly.length >= 7 }
    }
}
