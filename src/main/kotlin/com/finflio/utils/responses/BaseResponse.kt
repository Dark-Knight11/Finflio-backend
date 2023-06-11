package com.finflio.utils.responses

import io.ktor.http.*

interface BaseResponse {
    val status: Int
    val message: String
}

enum class State(val value: Int) {
    SUCCESS(HttpStatusCode.OK.value),
    CREATED(HttpStatusCode.Created.value),
    NOT_FOUND(HttpStatusCode.NotFound.value),
    FAILED(HttpStatusCode.BadRequest.value),
    CONFLICT(HttpStatusCode.Conflict.value),
    UNAUTHORIZED(HttpStatusCode.Unauthorized.value),
}