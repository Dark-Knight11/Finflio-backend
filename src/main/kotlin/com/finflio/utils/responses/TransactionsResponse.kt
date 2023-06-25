package com.finflio.utils.responses

import com.finflio.data.models.Transaction
import kotlinx.serialization.Serializable

@Serializable
class TransactionsResponse(
    override val status: Int,
    override val message: String,
    val monthTotal: Int = 0,
    val transactions: List<Transaction> = emptyList(),
    val totalPages: Int = 0,
) : BaseResponse {

    companion object {

        fun failed(message: String) = TransactionsResponse(
            State.NOT_FOUND.value,
            message
        )

        fun success(
            transactions: List<Transaction>,
            message: String,
            pageCount: Int,
            monthTotal: Int = 0
        ) = TransactionsResponse(
            State.SUCCESS.value,
            message,
            monthTotal,
            transactions,
            pageCount
        )
    }
}