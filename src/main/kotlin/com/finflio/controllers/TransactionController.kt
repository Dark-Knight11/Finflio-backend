package com.finflio.controllers

import com.finflio.models.Transaction
import com.finflio.repository.TransactionRepository
import com.finflio.utils.exceptions.FailureMessages
import com.finflio.utils.exceptions.RequestConflictException
import com.finflio.utils.requests.TransactionRequest
import com.finflio.utils.responses.TransactionResponse
import com.finflio.utils.responses.TransactionsResponse
import org.bson.types.ObjectId

class TransactionController(
    private val repository: TransactionRepository
) {
    suspend fun createTransaction(transactionRequest: TransactionRequest, userId: String): TransactionResponse {
        validateTransactionRequest(transactionRequest)

        val transaction = transactionRequest.toTransaction(userId)
        val response = repository.createTransaction(transaction)
        response?.let {
            return TransactionResponse.success(response, "Successful")
        }
        return TransactionResponse.failed(FailureMessages.MESSAGE_FAILED)
    }

    suspend fun getTransaction(transactionId: String): TransactionResponse {
        val response = repository.getTransaction(transactionId)
        response?.let {
            return TransactionResponse.success(response, "Successful")
        }
        return TransactionResponse.failed(FailureMessages.MESSAGE_FAILED)
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

    suspend fun getAllTransactions(userId: String): TransactionsResponse {
        val response = repository.getAllTransactions(userId)
        return if (response.isNotEmpty()) TransactionsResponse.success(response, "Successful")
        else TransactionsResponse.failed("No Transactions")
    }

    private fun TransactionRequest.toTransaction(userId: String, transactionId: String): Transaction {
        return Transaction(
            timestamp = timestamp,
            type = type,
            category = category,
            paymentMethod = paymentMethod,
            description = description,
            amount = amount,
            attachment = attachment,
            from = from,
            to = to,
            id = ObjectId(transactionId),
            userId = ObjectId(userId)
        )
    }

    private fun TransactionRequest.toTransaction(userId: String): Transaction {
        return Transaction(
            timestamp = timestamp,
            type = type,
            category = category,
            paymentMethod = paymentMethod,
            description = description,
            amount = amount,
            attachment = attachment,
            from = from,
            to = to,
            userId = ObjectId(userId)
        )
    }

    private fun validateTransactionRequest(transaction: TransactionRequest) {

        // Check if the timestamp is not empty
        if (transaction.timestamp.isEmpty()) {
            throw RequestConflictException("Timestamp cannot be empty")
        }

        // Check if the type is one of the allowed values
        if (transaction.type.isEmpty()) {
            throw RequestConflictException("Type cannot be empty")
        } else if (!listOf("Expense", "Income", "Unsettled Transaction").contains(transaction.type)) {
            throw RequestConflictException("Type must be one of the following: Expense, Income, Unsettled Transaction")
        }

        // Check if the category is not empty
        if (transaction.category.isEmpty()) {
            throw RequestConflictException("Category cannot be empty")
        }

        // Check if the paymentMethod is not empty
        if (transaction.paymentMethod.isEmpty()) {
            throw RequestConflictException("PaymentMethod cannot be empty")
        }

        // Check if the description is not empty
        if (transaction.description.isEmpty()) {
            throw RequestConflictException("Description cannot be empty")
        }

        // Check if the amount is greater than 0
        if (transaction.amount <= 0f) {
            throw RequestConflictException("Amount must be greater than 0")
        }

        // Check if the attachment is a cloudinary url
        if (transaction.attachment != null && !transaction.attachment.startsWith("https://res.cloudinary.com/")) {
            throw RequestConflictException("Attachment must be a cloudinary url")
        }

        // Check if the from and to fields are mutually exclusive
        if (transaction.from != null && transaction.to != null) {
            throw RequestConflictException("From and to fields are mutually exclusive")
        } else if (transaction.from == null && transaction.to == null) {
            throw RequestConflictException("One of the from and to fields must be filled")
        }

        // Check if the type is expense and the to field is not filled
        if (transaction.type == "Expense" && transaction.to == null) {
            throw RequestConflictException("To field must be filled for expense transactions")
        }

        // Check if the type is income and the from field is not filled
        if (transaction.type == "Income" && transaction.from == null) {
            throw RequestConflictException("From field must be filled for income transactions")
        }
    }
}