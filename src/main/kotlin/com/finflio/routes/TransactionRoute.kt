package com.finflio.routes

import com.finflio.controllers.TransactionController
import com.finflio.security.UserPrincipal
import com.finflio.utils.exceptions.FailureMessages
import com.finflio.utils.requests.TransactionRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Month

fun Route.TransactionRoute(transactionController: TransactionController) {

    route("/transaction") {
        authenticate {
            post {
                val request = runCatching { call.receive<TransactionRequest>() }.getOrElse {
                    throw BadRequestException(FailureMessages.MESSAGE_TRANSACTION_DETAILS_MISSING)
                }
                val principal =
                    call.principal<UserPrincipal>() ?: throw BadRequestException(FailureMessages.MESSAGE_FAILED)

                val response = transactionController.createTransaction(request, principal.userId)
                call.respond(HttpStatusCode.Created, response)
            }

            get {
                val id = call.parameters["id"] ?: kotlin.run {
                    throw BadRequestException("Transaction ID is missing")
                }
                call.principal<UserPrincipal>() ?: throw BadRequestException(FailureMessages.MESSAGE_FAILED)
                val response = transactionController.getTransaction(id)
                call.respond(HttpStatusCode.OK, response)
            }

            put {
                val request = runCatching { call.receive<TransactionRequest>() }.getOrElse {
                    throw BadRequestException(FailureMessages.MESSAGE_TRANSACTION_DETAILS_MISSING)
                }
                val transactionId = call.parameters["id"] ?: kotlin.run {
                    throw BadRequestException("Transaction ID is missing")
                }
                val principal =
                    call.principal<UserPrincipal>() ?: throw BadRequestException(FailureMessages.MESSAGE_FAILED)

                val response = transactionController.updateTransaction(request, principal.userId, transactionId)
                call.respond(HttpStatusCode.OK, response)
            }

            delete {
                val id = call.parameters["id"] ?: kotlin.run {
                    throw BadRequestException("Transaction ID is missing")
                }
                call.principal<UserPrincipal>() ?: throw BadRequestException(FailureMessages.MESSAGE_FAILED)

                val response = transactionController.deleteTransaction(id)
                call.respond(HttpStatusCode.OK, response)
            }

            get("/all") {
                val principal =
                    call.principal<UserPrincipal>() ?: throw BadRequestException(FailureMessages.MESSAGE_FAILED)

                val month = call.parameters["month"]
                val page = call.parameters["page"] ?: kotlin.run {
                    throw BadRequestException("Page no is missing")
                }

                if (month != null) {
                    val response = transactionController.getFilteredTransaction(
                        Month.valueOf(month),
                        principal.userId,
                        page.toInt()
                    )
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    val response = transactionController.getAllTransactions(principal.userId, page.toInt())
                    call.respond(HttpStatusCode.OK, response)
                }
            }

            get("/stats") {
                val principal =
                    call.principal<UserPrincipal>() ?: throw BadRequestException(FailureMessages.MESSAGE_FAILED)
                val response = transactionController.getStats(principal.userId)
                call.respond(HttpStatusCode.OK, response)
            }

            post("/all") {
                val request = runCatching { call.receive<List<TransactionRequest>>() }.getOrElse {
                    throw BadRequestException(FailureMessages.MESSAGE_TRANSACTION_DETAILS_MISSING)
                }
                val principal =
                    call.principal<UserPrincipal>() ?: throw BadRequestException(FailureMessages.MESSAGE_FAILED)
                val response = transactionController.postAll(request, principal.userId)
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}