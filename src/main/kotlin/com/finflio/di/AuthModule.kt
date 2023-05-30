package com.finflio.di

import com.finflio.controllers.AuthController
import com.finflio.repository.AuthRepository
import com.finflio.security.JwtService
import com.finflio.security.hashing.SHA256HashingService
import com.finflio.security.token.TokenConfig
import io.ktor.server.application.*
import org.koin.dsl.module

val authModule = module {
    single { AuthRepository(get()) }
    single { SHA256HashingService() }
    single { JwtService() }
    single { (application: Application) -> provideTokenConfig(application.environment)}
    single { (tokenConfig: TokenConfig) -> AuthController(get(), get(), get(), tokenConfig) }
}

private fun provideTokenConfig(environment: ApplicationEnvironment): TokenConfig {
    return TokenConfig(
        issuer = environment.config.property("jwt.domain").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 60L * 1000L, // 1 minute
        secret = System.getenv("JWT_SECRET")
    )
}