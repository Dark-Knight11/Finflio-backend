package com.finflio.utils.responses

import com.finflio.data.models.Transaction
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

        fun created(transaction: Transaction? = null, message: String) = TransactionResponse(
            State.CREATED.value,
            message,
            transaction
        )
    }
}

