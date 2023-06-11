package com.finflio.repository

import com.fasterxml.jackson.annotation.JsonFormat
import com.finflio.data.models.Stats
import com.finflio.data.models.StatsData
import com.finflio.data.models.Transaction
import io.ktor.server.util.*
import io.ktor.util.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
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

    @OptIn(InternalAPI::class)
    protected fun getMissingData(
        startOfWeek: Long,
        endOfWeek: Long,
        startDate: Long,
        endDate: Long,
        startOfYear: Long,
        endOfYear: Long,
        data: Stats?
    ): Stats? {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)

        val allWeekDates = generateSequence(startOfWeek) {
            it.plus(24 * 60 * 60 * 1000)
        }
            .takeWhile { it <= endOfWeek }
            .toList()

        val missingWeekDates = allWeekDates.map { formatter.format(Date(it)) }
            .filterNot { date ->
                data?.weeklyData?.any { it.date == date } == true
            }

        val allMonthDates = generateSequence(startDate) {
            it.plus(24 * 60 * 60 * 1000)
        }
            .takeWhile { it <= endDate }
            .toList()

        val missingMonthDates = allMonthDates.map { formatter.format(Date(it)) }
            .filterNot { date ->
                data?.monthlyData?.any { it.date == date } == true
            }

        val allMonths = generateSequence(Date(startOfYear).toLocalDateTime().toLocalDate()) {
            it.plusMonths(1)
        }.takeWhile { it <= Date(endOfYear).toLocalDateTime().toLocalDate() }
            .toList()

        val yearDataFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

        val missingMonths = allMonths.map { it.format(yearDataFormatter) }
            .filterNot { date ->
                data?.yearlyData?.any { it.date == date } == true
            }


        val defaultData = StatsData(0, 0, "")

        val updatedWeeklyData =
            data?.weeklyData?.plus(missingWeekDates.map { defaultData.copy(date = it) })?.sortedBy { it.date }
        val updatedMonthlyData =
            data?.monthlyData?.plus(missingMonthDates.map { defaultData.copy(date = it) })?.sortedBy { it.date }
        val updatedYearlyData =
            data?.yearlyData?.plus(missingMonths.map { defaultData.copy(date = it) })?.sortedBy { it.date }


        return data?.copy(
            weeklyData = updatedWeeklyData ?: emptyList(),
            monthlyData = updatedMonthlyData ?: emptyList(),
            yearlyData = updatedYearlyData ?: emptyList()
        )
    }
}