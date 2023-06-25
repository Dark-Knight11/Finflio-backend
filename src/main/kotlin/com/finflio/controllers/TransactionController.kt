package com.finflio.controllers

import com.finflio.data.models.Transaction
import com.finflio.repository.TransactionRepository
import com.finflio.utils.exceptions.FailureMessages
import com.finflio.utils.requests.TransactionRequest
import com.finflio.utils.responses.StatsResponse
import com.finflio.utils.responses.TransactionResponse
import com.finflio.utils.responses.TransactionsResponse
import java.time.Month

class TransactionController(
    private val repository: TransactionRepository
) : ControllerUtils() {
    suspend fun createTransaction(transactionRequest: TransactionRequest, userId: String): TransactionResponse {
        validateTransactionRequest(transactionRequest)

        val transaction = transactionRequest.toTransaction(userId)
        val response = repository.createTransaction(transaction)
        response?.let {
            return TransactionResponse.created(response, "Successful")
        }
        return TransactionResponse.failed(FailureMessages.MESSAGE_FAILED)
    }

    suspend fun getTransaction(transactionId: String): TransactionResponse {
        val response = repository.getTransaction(transactionId)
        response?.let {
            return TransactionResponse.success(response, "Successful")
        }
        return TransactionResponse.failed(FailureMessages.MESSAGE_TRANSACTION_NOT_FOUND)
    }

    suspend fun updateTransaction(
        transactionRequest: TransactionRequest,
        userId: String,
        transactionId: String
    ): TransactionResponse {
        validateTransactionRequest(transactionRequest)

        val transaction = transactionRequest.toTransaction(userId, transactionId)
        val response = repository.updateTransaction(transaction)
        response?.let {
            return TransactionResponse.success(response, "Successful")
        }
        return TransactionResponse.failed(FailureMessages.MESSAGE_FAILED)
    }

    suspend fun deleteTransaction(transactionId: String): TransactionResponse {
        val wasAcknowledged = repository.deleteTransaction(transactionId)
        return if (wasAcknowledged) TransactionResponse.success(message = "Transaction Deleted Successfully")
        else TransactionResponse.failed(FailureMessages.MESSAGE_FAILED)
    }

    suspend fun getUnsettledTransactions(userId: String, pageNo: Int): TransactionsResponse {
        val response = repository.getUnsettledTransactions(userId, pageNo)
        return if (response.first.isNotEmpty()) TransactionsResponse.success(
            response.first,
            "Successful",
            response.second
        )
        else TransactionsResponse.failed("No Transactions")
    }

    suspend fun getFilteredTransaction(month: Month, userId: String, pageNo: Int): TransactionsResponse {
        val response = repository.getFilteredTransaction(month, userId, pageNo)
        return if (response.first.isNotEmpty()) TransactionsResponse.success(
            response.first,
            "Successful",
            response.second,
            response.third
        )
        else TransactionsResponse.failed("No Transactions")
    }

    suspend fun getStats(userId: String): StatsResponse {
        val response = repository.getStats(userId)
        return if (response != null) StatsResponse.success(
            message = "Successful",
            stats = response
        ) else StatsResponse.failed(FailureMessages.MESSAGE_FAILED)
    }

    suspend fun postAll(transactions: List<TransactionRequest>, userId: String): TransactionsResponse {
        val newList = mutableListOf<Transaction>()
        transactions.forEach {
            newList.add(it.toTransaction(userId))
        }

        val response = repository.postAll(newList)
        return if (response) TransactionsResponse.success(newList, "Successful", 0)
        else TransactionsResponse.failed(FailureMessages.MESSAGE_FAILED)

    }
}