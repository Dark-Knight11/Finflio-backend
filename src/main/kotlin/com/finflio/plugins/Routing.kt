package com.finflio.plugins

import com.finflio.controllers.AuthController
import com.finflio.controllers.TransactionController
import com.finflio.routes.AuthRoute
import com.finflio.routes.TransactionRoute
import com.finflio.security.TokenConfig
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.parameter.parametersOf
import org.koin.ktor.ext.inject

fun Application.configureRouting() {

    val tokenConfig: TokenConfig by inject { parametersOf(this@configureRouting) }
    val authController: AuthController by inject { parametersOf(tokenConfig) }
    val transactionController by inject<TransactionController>()

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        AuthRoute(authController)
        TransactionRoute(transactionController)
    }

}