package com.finflio.utils.responses

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    override val status: Int,
    override val message: String,
    val token: String? = null
) : BaseResponse {

    companion object {

        fun failed(message: String) = AuthResponse(
            State.FAILED.value,
            message
        )

        fun success(token: String? = null, message: String) = AuthResponse(
            State.SUCCESS.value,
            message,
            token
        )
    }
}
