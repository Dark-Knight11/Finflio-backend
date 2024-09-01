package com.finflio

import com.finflio.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = (System.getenv("PORT")?:"5000").toInt(), host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureKoin()
    configureHTTP()
    configureSecurity()
    configureSerialization()
    configureMonitoring()
    configureRouting()
    configureStatusPage()
}
