package com.finflio.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class JwtService() {
    fun generateToken(config: TokenConfig, userId: String): String =
        JWT.create()
            .withIssuer(config.issuer)
            .withSubject("Authentication")
            .withAudience(config.audience)
            .withExpiresAt(Date(System.currentTimeMillis() + config.expiresIn))
            .withClaim(JWT_CLAIM, userId)
            .sign(Algorithm.HMAC256(config.secret))

    companion object {
        const val JWT_CLAIM = "userId"
    }
}