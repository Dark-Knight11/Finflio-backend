package com.finflio.repository

import com.finflio.models.Transaction
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class TransactionRepository(db: CoroutineDatabase) {
    private val transactions = db.getCollection<Transaction>()

    suspend fun createTransaction(transaction: Transaction): Transaction? {
        val wasAcknowledged = transactions.insertOne(transaction).wasAcknowledged()
        return if (wasAcknowledged) transaction else null
    }

    suspend fun getTransaction(id: String): Transaction? =
        transactions.findOneById(ObjectId(id))

    suspend fun updateTransaction(transaction: Transaction): Transaction? {
        val wasAcknowledged = transactions.updateOneById(transaction.id, transaction).wasAcknowledged()
        return if (wasAcknowledged) transaction else null
    }

    suspend fun deleteTransaction(id: String): Boolean =
        transactions.deleteOneById(ObjectId(id)).wasAcknowledged()

    suspend fun getAllTransactions(userId: String): List<Transaction> =
        transactions.find(Transaction::userId eq ObjectId(userId)).toList()
}