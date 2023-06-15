package com.finflio.controllers

import com.finflio.data.models.Transaction
import com.finflio.utils.exceptions.RequestConflictException
import com.finflio.utils.requests.LoginRequest
import com.finflio.utils.requests.RegisterUserRequest
import com.finflio.utils.requests.TransactionRequest
import org.bson.types.ObjectId
import java.util.regex.Pattern

open class ControllerUtils {

    protected fun validateSignUpCredentialsOrThrowException(request: RegisterUserRequest) {
        with(request) {
            val message = when {
                (email.isBlank() or password.isBlank() or name.isBlank()) -> "Email, password and name should not be blank"
                (password.length !in (8..50)) -> "Password should be of min 8 and max 50 character in length"
                (name.length !in (4..24)) -> "Name should be of min 4 and max 24 character in length"
                (!Pattern.matches(EMAIL_REGEX, email)) -> "Invalid Email"
                else -> return
            }
            throw RequestConflictException(message)
        }
    }

    protected fun validateLoginCredentialsOrThrowException(request: LoginRequest) {
        with(request) {
            val message = when {
                (email.isBlank() or password.isBlank() or password.isBlank()) -> "email and password should not be blank"
                (password.length !in (8..50)) -> "Password should be of min 8 and max 50 character in length"
                (!Pattern.matches(EMAIL_REGEX, email)) -> "Invalid Email"
                else -> return
            }
            throw RequestConflictException(message)
        }
    }

    companion object {
        const val EMAIL_REGEX =
            "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"

    }

    protected fun TransactionRequest.toTransaction(userId: String, transactionId: String): Transaction {
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

    protected fun TransactionRequest.toTransaction(userId: String): Transaction {
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

    protected fun validateTransactionRequest(transaction: TransactionRequest) {

        // Check if the timestamp is not empty
        if (transaction.timestamp.toString().isEmpty()) {
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
        if (!transaction.from.isNullOrBlank() && !transaction.to.isNullOrBlank()) {
            throw RequestConflictException("From and to fields are mutually exclusive")
        } else if (transaction.from.isNullOrBlank() && transaction.to.isNullOrBlank()) {
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