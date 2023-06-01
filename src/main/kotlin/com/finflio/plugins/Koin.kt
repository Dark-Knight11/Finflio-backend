package com.finflio.plugins

import com.finflio.di.authModule
import com.finflio.di.controllerModule
import com.finflio.di.dbModule
import com.finflio.di.repositoryModule
import io.ktor.server.application.*
import org.koin.core.logger.Level
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.KoinApplicationStarted
import org.koin.ktor.plugin.KoinApplicationStopPreparing
import org.koin.ktor.plugin.KoinApplicationStopped
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger(level = Level.INFO)
        modules(listOf(dbModule, repositoryModule, authModule, controllerModule))

        environment.monitor.subscribe(KoinApplicationStarted) {
            log.info("Koin started.")
        }

        environment.monitor.subscribe(KoinApplicationStopPreparing) {
            log.info("Koin stopping...")
        }

        environment.monitor.subscribe(KoinApplicationStopped) {
            log.info("Koin stopped.")
        }
    }
}