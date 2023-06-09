package com.finflio.repository

import com.fasterxml.jackson.annotation.JsonFormat
import com.finflio.data.models.Transaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.temporal.WeekFields
import java.util.*

open class RepositoryUtils {

    infix fun Int.fdiv(i: Int): Double = this / i.toDouble()

    data class Test(
        @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY]) val count: Long,
        val transactions: List<Transaction>?
    )

    protected fun getStartAndEndTimestamps(year: Int, month: Int): Pair<Long, Long> {
        val startOfMonth = LocalDate.of(year, month, 1)
        val endOfMonth = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth())

        val startDate = startOfMonth.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val endDate = endOfMonth.atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli()

        return startDate to endDate
    }

    protected fun getCurrentYearDates(year: Int): Pair<Long, Long> {
        val startOfMonth = LocalDate.of(year, 1, 1)
        val endOfMonth = LocalDate.of(year, 12, 31)

        val startDate = startOfMonth.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val endDate = endOfMonth.atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli()

        return startDate to endDate
    }

    protected fun getCurrentWeekDates(date: LocalDateTime): Pair<Long, Long> {
        val startOfWeek = date.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()

        val endOfWeek = date.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 7)
            .withHour(23)
            .withMinute(59)
            .withSecond(59)
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()

        return startOfWeek to endOfWeek
    }
}