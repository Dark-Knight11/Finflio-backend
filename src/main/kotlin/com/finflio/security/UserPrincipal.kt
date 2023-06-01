package com.finflio.security

import io.ktor.server.auth.*

class UserPrincipal(val userId: String) : Principal