package com.finflio.routes

import com.finflio.controllers.AuthController
import com.finflio.utils.exceptions.FailureMessages
import com.finflio.utils.requests.LoginRequest
import com.finflio.utils.requests.RegisterUserRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Registers authentication routes.
 *
 * @param authController An instance of [AuthController].
 */
fun Route.AuthRoute(authController: AuthController) {
    /**
     * Defines a route group for the "/auth" path.
     */
    route("/auth") {
        /**
         * Handles the POST request to register a new user.
         *
         * @throws [BadRequestException] if the request body is missing or invalid.
         * @param block The route handler block.
         */
        post("/register") {
            val request = runCatching { call.receive<RegisterUserRequest>() }.getOrElse {
                throw BadRequestException(FailureMessages.MESSAGE_MISSING_REGISTRATION_DATA)
            }
            val response = authController.register(request)
            call.respond(HttpStatusCode.OK, response)
        }

        /**
         * Handles the POST request to perform user login.
         *
         * @throws [BadRequestException] if the request body is missing or invalid.
         * @param block The route handler block.
         */
        post("/login") {
            val request = runCatching { call.receive<LoginRequest>() }.getOrElse {
                throw BadRequestException(FailureMessages.MESSAGE_MISSING_CREDENTIALS)
            }
            val response = authController.login(request)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}