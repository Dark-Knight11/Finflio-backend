package com.finflio.utils.exceptions

object FailureMessages {
    const val MESSAGE_MISSING_CREDENTIALS = "Required 'email' or 'password' missing."
    const val MESSAGE_MISSING_REGISTRATION_DATA = "Required 'name' or 'email' or 'password' missing"
    const val MESSAGE_INCORRECT_CREDENTIALS = "Incorrect 'email' or 'password'"
    const val MESSAGE_ACCESS_DENIED = "Access Denied!"
    const val MESSAGE_FAILED = "Something went wrong!"

    const val MESSAGE_TRANSACTION_DETAILS_MISSING = "Invalid Transaction"
    const val MESSAGE_TRANSACTION_NOT_FOUND = "Transaction not Found. Invalid ID"
}