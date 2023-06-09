package com.finflio.repository

import com.finflio.data.models.User
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class AuthRepository(db: CoroutineDatabase) {
    private val users = db.getCollection<User>()

    suspend fun findUserByEmail(email: String): User? =
        users.findOne(User::email eq email)

    suspend fun createUser(user: User): Boolean = users.insertOne(user).wasAcknowledged()
}