package com.finflio

import com.cloudinary.Cloudinary
import com.finflio.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val cloudinary = Cloudinary("cloudinary://343346658476441:2cXEvtX696E-upekMaNGxKi9Rtk@deubsgtl4")
    configureKoin()
    configureHTTP()
    configureSecurity()
    configureSerialization()
    configureMonitoring()
    configureRouting(cloudinary)
    configureStatusPage()
}
