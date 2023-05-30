package com.finflio.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.finflio.security.token.TokenClaim
import com.finflio.security.token.TokenConfig
import java.util.*

class JwtService() {
    fun generateToken(config: TokenConfig, claim: TokenClaim): String =
        JWT.create()
            .withIssuer(config.issuer)
            .withSubject("Authentication")
            .withAudience(config.audience)
            .withExpiresAt(Date(System.currentTimeMillis() + config.expiresIn))
            .withClaim(claim.name, claim.value)
            .sign(Algorithm.HMAC256(config.secret))
}