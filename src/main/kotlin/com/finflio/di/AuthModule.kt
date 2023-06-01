package com.finflio.di

import com.finflio.security.JwtService
import com.finflio.security.TokenConfig
import com.finflio.security.hashing.SHA256HashingService
import io.ktor.server.application.*
import org.koin.dsl.module

val authModule = module {
    single { SHA256HashingService() }
    single { JwtService() }
    single { (application: Application) -> provideTokenConfig(application.environment)}
}

private fun provideTokenConfig(environment: ApplicationEnvironment): TokenConfig {
    return TokenConfig(
        issuer = environment.config.property("jwt.domain").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 30L * 24L * 60L * 60L * 1000L, // 30 days
        secret = System.getenv("JWT_SECRET")
    )
}