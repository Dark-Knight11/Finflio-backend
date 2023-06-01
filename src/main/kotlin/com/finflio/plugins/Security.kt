package com.finflio.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.finflio.security.JwtService.Companion.JWT_CLAIM
import com.finflio.security.UserPrincipal
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    
    authentication {
            jwt {
                val jwtAudience = this@configureSecurity.environment.config.property("jwt.audience").getString()
                val jwtIssuer = this@configureSecurity.environment.config.property("jwt.domain").getString()
                realm = this@configureSecurity.environment.config.property("jwt.realm").getString()
                verifier(
                    JWT
                        .require(Algorithm.HMAC256(System.getenv("JWT_SECRET")))
                        .withAudience(jwtAudience)
                        .withIssuer(jwtIssuer)
                        .build()
                )
                validate { credential ->
                    val userId = credential.payload.getClaim(JWT_CLAIM)
                    if (!userId.isMissing and !userId.isNull) UserPrincipal(userId.asString()) else null
                }
            }
        }
}
