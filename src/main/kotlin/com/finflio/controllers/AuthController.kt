package com.finflio.controllers

import com.finflio.data.models.User
import com.finflio.repository.AuthRepository
import com.finflio.security.JwtService
import com.finflio.security.TokenConfig
import com.finflio.security.hashing.SHA256HashingService
import com.finflio.security.hashing.SaltedHash
import com.finflio.utils.exceptions.FailureMessages
import com.finflio.utils.requests.LoginRequest
import com.finflio.utils.requests.RegisterUserRequest
import com.finflio.utils.responses.AuthResponse
import io.ktor.server.plugins.*

class AuthController(
    private val authRepository: AuthRepository,
    private val hashingService: SHA256HashingService,
    private val tokenService: JwtService,
    private val tokenConfig: TokenConfig
): ControllerUtils() {
    suspend fun register(request: RegisterUserRequest): AuthResponse {
        validateSignUpCredentialsOrThrowException(request)

        val saltedHash = hashingService.generateHash(request.password)
        val user = User(
            email = request.email,
            password = saltedHash.hash,
            name = request.name,
            salt = saltedHash.salt
        )

        val wasAcknowledged = authRepository.createUser(user = user)
        if (!wasAcknowledged) {
            return AuthResponse.failed("Error Creating the user")
        }

        return AuthResponse.success(message = "Registration Successful")
    }


    suspend fun login(loginRequest: LoginRequest): AuthResponse {
        validateLoginCredentialsOrThrowException(loginRequest)

        val user = authRepository.findUserByEmail(loginRequest.email)
            ?: throw BadRequestException(FailureMessages.MESSAGE_INCORRECT_CREDENTIALS)

        val isValidPassword = hashingService.verify(
            plainText = loginRequest.password, saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )

        if (!isValidPassword) {
            throw BadRequestException(FailureMessages.MESSAGE_INCORRECT_CREDENTIALS)
        }
        val token = tokenService.generateToken(
            config = tokenConfig,
            userId = user.id.toString()
        )

        return AuthResponse.success(token, "Login Successful")
    }
}

