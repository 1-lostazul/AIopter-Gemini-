package com.example.security

import java.util.regex.Pattern

/**
 * Redacts passwords, auth codes, credit cards, SSNs, API tokens, and secret keys
 * before logging or storing metadata.
 */
object DataSanitizer {

    private val API_KEY_PATTERN = Pattern.compile("(?i)(AIza[0-9A-Za-z-_]{35}|sk-[A-Za-z0-9]{32,}|Bearer\\s+[A-Za-z0-9._~+/-]+=*)")
    private val CARD_PATTERN = Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b")
    private val AUTH_CODE_PATTERN = Pattern.compile("(?i)\\b(code|pin|otp|token|password)[:=\\s]+([0-9A-Za-z]{4,8})\\b")
    private val EMAIL_PATTERN = Pattern.compile("(?i)[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private val SSN_PATTERN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b")

    fun redact(input: String?): String {
        if (input.isNullOrBlank()) return ""

        var sanitized = input
        sanitized = API_KEY_PATTERN.matcher(sanitized).replaceAll("[REDACTED_API_KEY]")
        sanitized = CARD_PATTERN.matcher(sanitized).replaceAll("[REDACTED_CARD_NUMBER]")
        sanitized = AUTH_CODE_PATTERN.matcher(sanitized).replaceAll("$1:[REDACTED]")
        sanitized = SSN_PATTERN.matcher(sanitized).replaceAll("[REDACTED_SSN]")

        return sanitized
    }

    fun maskParameterValue(key: String, value: String): String {
        val lowerKey = key.lowercase()
        return if (lowerKey.contains("password") ||
            lowerKey.contains("secret") ||
            lowerKey.contains("token") ||
            lowerKey.contains("key") ||
            lowerKey.contains("card") ||
            lowerKey.contains("cvv") ||
            lowerKey.contains("pin")
        ) {
            "••••••••"
        } else {
            redact(value)
        }
    }
}
