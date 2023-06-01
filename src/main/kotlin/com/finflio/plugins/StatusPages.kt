package com.finflio.plugins

import com.finflio.utils.exceptions.FailureMessages
import com.finflio.utils.exceptions.RequestConflictException
import com.finflio.utils.responses.FailureResponse
import com.finflio.utils.responses.State
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPage() {
    install(StatusPages) {
        exception<BadRequestException> { call, status ->
            call.respond(
                HttpStatusCode.BadRequest,
                FailureResponse(State.FAILED.value, status.message ?: "Bad Request")
            )
        }

        exception<RequestConflictException> { call, status ->
            call.respond(HttpStatusCode.Conflict, FailureResponse(State.CONFLICT.value, status.message))
        }

        status(HttpStatusCode.Unauthorized) { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                FailureResponse(State.UNAUTHORIZED.value, FailureMessages.MESSAGE_ACCESS_DENIED)
            )
        }

        status(HttpStatusCode.MethodNotAllowed) { call, _ ->
            call.respond(
                HttpStatusCode.MethodNotAllowed,
                FailureResponse(HttpStatusCode.MethodNotAllowed.value, "Method Not Allowed")
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                FailureResponse(
                    HttpStatusCode.InternalServerError.value,
                    cause.message ?: FailureMessages.MESSAGE_FAILED
                )
            )
        }
    }
}