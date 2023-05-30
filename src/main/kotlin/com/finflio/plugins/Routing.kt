package com.finflio.plugins

import com.finflio.controllers.AuthController
import com.finflio.routes.AuthRoute
import com.finflio.security.token.TokenConfig
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.parameter.parametersOf
import org.koin.ktor.ext.inject

fun Application.configureRouting() {

    val tokenConfig: TokenConfig by inject { parametersOf(this@configureRouting) }
    val authController: AuthController by inject { parametersOf(tokenConfig) }

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        AuthRoute(authController)
    }
}
