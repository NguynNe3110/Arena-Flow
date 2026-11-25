@file:Suppress("MagicNumber")

package com.igorwojda.showcase.feature.base.common.time

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Vietnam local datetime utilities (UTC+7, offset-free ISO-8601) per SYSTEM-002 API_Spec.
 * Format: YYYY-MM-DDTHH:mm:ss (never append 'Z' or '+07:00' in requests).
 */
object VietnamDateTime {
    private val vietnamZone: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")
    private val vietnamOffset: ZoneOffset = ZoneOffset.ofHours(7)
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    fun nowIso(clockOverride: String? = null): String {
        if (!clockOverride.isNullOrBlank()) {
            val parsed = parseToLocalDateTime(clockOverride)
            if (parsed != null) return parsed.format(formatter)
        }
        return LocalDateTime.now(vietnamZone).format(formatter)
    }

    fun formatLocal(dateTime: LocalDateTime): String = dateTime.format(formatter)

    fun parseToLocalDateTime(value: String): LocalDateTime? =
        runCatching {
            val trimmed = value.trim()
            if (trimmed.endsWith("Z") || trimmed.contains("+") || trimmed.drop(10).contains("-")) {
                val instant = OffsetDateTime.parse(trimmed).toInstant()
                LocalDateTime.ofInstant(instant, vietnamZone)
            } else {
                LocalDateTime.parse(trimmed)
            }
        }.getOrNull()

    fun parseToEpochMillis(value: String): Long? = parseToLocalDateTime(value)?.toInstant(vietnamOffset)?.toEpochMilli()

    fun currentEpochMillis(clockOverride: String? = null): Long = clockOverride?.let(::parseToEpochMillis) ?: Instant.now().toEpochMilli()
}
