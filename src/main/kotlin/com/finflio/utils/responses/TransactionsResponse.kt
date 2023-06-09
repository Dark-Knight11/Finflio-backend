package com.finflio.utils.responses

import com.finflio.data.models.Transaction
import kotlinx.serialization.Serializable

@Serializable
class TransactionsResponse(
    override val status: Int,
    override val message: String,
    val transactions: List<Transaction> = emptyList(),
    val totalPages: Int = 0
) : BaseResponse {

    companion object {

        fun failed(message: String) = TransactionsResponse(
            State.FAILED.value,
            message
        )

        fun success(transactions: List<Transaction>, message: String, pageCount: Int) = TransactionsResponse(
            State.SUCCESS.value,
            message,
            transactions,
            pageCount
        )
    }
}