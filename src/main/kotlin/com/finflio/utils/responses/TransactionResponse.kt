package com.finflio.utils.responses

import com.finflio.models.Transaction
import kotlinx.serialization.Serializable

@Serializable
class TransactionResponse(
    override val status: Int,
    override val message: String,
    val transaction: Transaction? = null
) : BaseResponse {

    companion object {

        fun failed(message: String) = TransactionResponse(
            State.FAILED.value,
            message
        )

        fun success(transaction: Transaction? = null, message: String) = TransactionResponse(
            State.SUCCESS.value,
            message,
            transaction
        )
    }
}

@Serializable
class TransactionsResponse(
    override val status: Int,
    override val message: String,
    val transactions: List<Transaction> = emptyList()
) : BaseResponse {

    companion object {

        fun failed(message: String) = TransactionsResponse(
            State.FAILED.value,
            message
        )

        fun success(transactions: List<Transaction>, message: String) = TransactionsResponse(
            State.SUCCESS.value,
            message,
            transactions
        )
    }
}