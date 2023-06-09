package com.finflio.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Stats(
    val totalExpenseWeekly: Float,
    val totalExpenseMonthly: Float,
    val totalExpenseYearly: Float,
    val totalIncomeWeekly: Float,
    val totalIncomeMonthly: Float,
    val totalIncomeYearly: Float,
)
