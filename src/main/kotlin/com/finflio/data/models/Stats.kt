package com.finflio.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Stats(
    val weeklyData: List<StatsData>,
    val yearlyData: List<StatsData>,
    val monthlyData: List<StatsData>
)

@Serializable
data class StatsData(
    val totalDailyExpense: Int,
    val totalDailyIncome: Int,
    val date: String,
)