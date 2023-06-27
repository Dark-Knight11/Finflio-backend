package com.finflio.routes

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.finflio.controllers.TransactionController
import com.finflio.security.UserPrincipal
import com.finflio.utils.exceptions.FailureMessages
import com.finflio.utils.requests.TransactionRequest
import com.finflio.utils.responses.State
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Month

fun Route.TransactionRoute(transactionController: TransactionController, cloudinary: Cloudinary) {

    route("/transaction") {
        authenticate {
            post {
                val principal =
                    call.principal<UserPrincipal>() ?: throw BadRequestException(FailureMessages.MESSAGE_FAILED)

                var tempFilePath: String? = null
                val map = HashMap<String, String?>()

                val multipartData = call.receiveMultipart()
                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            map[part.name.toString()] = part.value
                        }

                        is PartData.FileItem -> {
                            val fileBytes = part.streamProvider().readBytes()
                            try {
                                val tempFile =
                                    Files.createTempFile(
                                        Paths.get(System.getProperty("java.io.tmpdir")),
                                        "upload",
                                        ".tmp"
                                    )
                                        .toFile()
                                tempFilePath = tempFile.absolutePath
                                tempFile.writeBytes(fileBytes)

                                // Upload the file to Cloudinary
                                map["attachment"] = cloudinary.uploader().upload(
                                    tempFilePath,
                                    ObjectUtils.asMap("folder", "finflio")
                                )["secure_url"] as String
                            } catch (e: Exception) {
                                println(e.message)
                            } finally {
                                // Clean up temporary file
                                tempFilePath?.let { tempFilePath ->
                                    val file = File(tempFilePath)
                                    if (file.exists()) {
                                        file.delete()
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                    part.dispose()
                }
                val request = TransactionRequest(
                    timestamp = map["timestamp"].toString().toLong(),
                    type = map["type"].toString(),
                    category = map["category"].toString(),
                    paymentMethod = map["paymentMethod"].toString(),
                    description = map["description"].toString(),
                    amount = map["amount"].toString().toFloat(),
                    attachment = map["attachment"],
                    from = map["from"],
                    to = map["to"]
                )
                println(request)
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
                val transactionId = call.parameters["id"] ?: kotlin.run {
                    throw BadRequestException("Transaction ID is missing")
                }
                val principal =
                    call.principal<UserPrincipal>() ?: throw BadRequestException(FailureMessages.MESSAGE_FAILED)

                var tempFilePath: String? = null
                val map = HashMap<String, String?>()

                val multipartData = call.receiveMultipart()
                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            map[part.name.toString()] = part.value
                        }

                        is PartData.FileItem -> {
                            val fileBytes = part.streamProvider().readBytes()
                            try {
                                val tempFile =
                                    Files.createTempFile(
                                        Paths.get(System.getProperty("java.io.tmpdir")),
                                        "upload",
                                        ".tmp"
                                    )
                                        .toFile()
                                tempFilePath = tempFile.absolutePath
                                tempFile.writeBytes(fileBytes)

                                // Upload the file to Cloudinary
                                map["attachment"] = cloudinary.uploader()
                                    .upload(tempFilePath, ObjectUtils.asMap("folder", "finflio"))["secure_url"] as String
                            } catch (e: Exception) {
                                println(e.message)
                            } finally {
                                // Clean up temporary file
                                tempFilePath?.let { tempFilePath ->
                                    val file = File(tempFilePath)
                                    if (file.exists()) {
                                        file.delete()
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                    part.dispose()
                }

                val request = TransactionRequest(
                    timestamp = map["timestamp"].toString().toLong(),
                    type = map["type"].toString(),
                    category = map["category"].toString(),
                    paymentMethod = map["paymentMethod"].toString(),
                    description = map["description"].toString(),
                    amount = map["amount"].toString().toFloat(),
                    attachment = map["attachment"],
                    from = map["from"],
                    to = map["to"]
                )
                println(request)
                val response = transactionController.updateTransaction(request, principal.userId, transactionId)
                call.respond(HttpStatusCode.OK, response)
            }

            delete {
                val id = call.parameters["id"] ?: kotlin.run {
                    throw BadRequestException("Transaction ID is missing")
                }
                val imageId = call.parameters["imageId"]
                imageId?.let {
                    cloudinary.uploader().destroy(it, ObjectUtils.emptyMap())
                }
                call.principal<UserPrincipal>() ?: throw BadRequestException(FailureMessages.MESSAGE_FAILED)

                val response = transactionController.deleteTransaction(id)
                call.respond(HttpStatusCode.OK, response)
            }

            get("/all") {
                val principal =
                    call.principal<UserPrincipal>() ?: throw BadRequestException(FailureMessages.MESSAGE_FAILED)

                val month = call.parameters["month"] ?: kotlin.run {
                    throw BadRequestException("Month is missing")
                }
                val page = call.parameters["page"] ?: kotlin.run {
                    throw BadRequestException("Page no is missing")
                }
                val response = transactionController.getFilteredTransaction(
                    Month.valueOf(month),
                    principal.userId,
                    page.toInt()
                )
                if (response.status == State.NOT_FOUND.value)
                    call.respond(HttpStatusCode.NotFound, response)
                else
                    call.respond(HttpStatusCode.OK, response)
            }

            get("/unsettled") {
                val principal =
                    call.principal<UserPrincipal>() ?: throw BadRequestException(FailureMessages.MESSAGE_FAILED)

                println(principal)
                val page = call.parameters["page"] ?: kotlin.run {
                    throw BadRequestException("Page no is missing")
                }
                val response = transactionController.getUnsettledTransactions(principal.userId, page.toInt())
                if (response.status == State.NOT_FOUND.value)
                    call.respond(HttpStatusCode.NotFound, response)
                else
                    call.respond(HttpStatusCode.OK, response)
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