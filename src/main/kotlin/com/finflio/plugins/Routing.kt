package com.finflio.plugins

import com.cloudinary.Cloudinary
import com.finflio.controllers.AuthController
import com.finflio.controllers.TransactionController
import com.finflio.routes.AuthRoute
import com.finflio.routes.TransactionRoute
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting(cloudinary: Cloudinary) {

    val authController by inject<AuthController>()
    val transactionController by inject<TransactionController>()

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        AuthRoute(authController)
        TransactionRoute(transactionController, cloudinary)
    }

}