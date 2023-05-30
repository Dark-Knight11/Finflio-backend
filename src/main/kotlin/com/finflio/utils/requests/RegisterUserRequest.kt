package com.finflio.utils.requests

import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserRequest(
    val name: String,
    val email: String,
    val password: String
)