package com.finflio.controllers

import com.finflio.models.User
import com.finflio.repository.AuthRepository
import com.finflio.security.JwtService
import com.finflio.security.hashing.SHA256HashingService
import com.finflio.security.hashing.SaltedHash
import com.finflio.security.token.TokenClaim
import com.finflio.security.token.TokenConfig
import com.finflio.utils.exceptions.FailureMessages
import com.finflio.utils.exceptions.RequestConflictException
import com.finflio.utils.requests.LoginRequest
import com.finflio.utils.requests.RegisterUserRequest
import com.finflio.utils.responses.AuthResponse
import io.ktor.server.plugins.*
import java.util.regex.Pattern

class AuthController(
    private val authRepository: AuthRepository,
    private val hashingService: SHA256HashingService,
    private val tokenService: JwtService,
    private val tokenConfig: TokenConfig
) {
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
            TokenClaim(name = "userId", value = user.id.toString())
        )

        return AuthResponse.success(token, "Login Successful")
    }

    private fun validateSignUpCredentialsOrThrowException(request: RegisterUserRequest) {
        with(request) {
            val message = when {
                (email.isBlank() or password.isBlank() or name.isBlank()) -> "Email, password and name should not be blank"
                (password.length !in (8..50)) -> "Password should be of min 8 and max 50 character in length"
                (name.length !in (4..24)) -> "Name should be of min 4 and max 24 character in length"
                (!Pattern.matches(EMAIL_REGEX, email)) -> "Invalid Email"
                else -> return
            }
            throw RequestConflictException(message)
        }
    }

    private fun validateLoginCredentialsOrThrowException(request: LoginRequest) {
        with(request) {
            val message = when {
                (email.isBlank() or password.isBlank() or password.isBlank()) -> "email and password should not be blank"
                (password.length !in (8..50)) -> "Password should be of min 8 and max 50 character in length"
                (!Pattern.matches(EMAIL_REGEX, email)) -> "Invalid Email"
                else -> return
            }
            throw RequestConflictException(message)
        }
    }

    companion object {
        const val EMAIL_REGEX =
            "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"

    }
}

