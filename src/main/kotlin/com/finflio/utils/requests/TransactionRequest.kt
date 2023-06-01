package com.finflio.utils.requests

import kotlinx.serialization.Serializable

@Serializable
data class TransactionRequest(
    val timestamp: String,
    val type: String,
    val category: String,
    val paymentMethod: String,
    val description: String,
    val amount: Float,
    val attachment: String? = null,
    val from: String? = null,
    val to: String? = null
)