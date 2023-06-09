package com.finflio.repository

import com.finflio.data.models.Stats
import com.finflio.data.models.Transaction
import com.mongodb.client.model.Aggregates.count
import com.mongodb.client.model.Facet
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.aggregate
import java.time.LocalDateTime
import java.time.Month
import kotlin.math.ceil

class TransactionRepository(db: CoroutineDatabase) : RepositoryUtils() {
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

    suspend fun getAllTransactions(
        userId: String,
        pageNo: Int,
        size: Int = 10
    ): Pair<List<Transaction>, Int> {
        transactions.find(Transaction::userId eq ObjectId(userId)).toList()
        val count = transactions.aggregate<Transaction>(
            match(Transaction::userId eq ObjectId(userId))
        ).toList().size

        val totalPages = ceil(count.fdiv(size)).toInt()

        return transactions
            .find(Transaction::userId eq ObjectId(userId))
            .skip((pageNo - 1) * size)
            .limit(size)
            .sort(ascending(Transaction::timestamp))
            .toList() to totalPages
    }


    suspend fun getFilteredTransaction(
        month: Month,
        userId: String,
        pageNo: Int,
        size: Int = 10
    ): Pair<List<Transaction>, Int> {
        val currentYear = LocalDateTime.now().year // TODO() take user input for year
        val (startDate, endDate) = getStartAndEndTimestamps(currentYear, month.value)

        val count = transactions.aggregate<Transaction>(
            match(Transaction::userId eq ObjectId(userId)),
            match(
                Transaction::timestamp gte startDate,
                Transaction::timestamp lte endDate
            )
        ).toList().size

        val totalPages = ceil(count.fdiv(size)).toInt()

        return transactions.aggregate<Transaction>(
            match(Transaction::userId eq ObjectId(userId)),
            match(
                Transaction::timestamp gte startDate,
                Transaction::timestamp lte endDate
            ),
            skip((pageNo - 1) * size),
            limit(size),
            sort(ascending(Transaction::timestamp))
        ).toList() to totalPages
    }

    suspend fun getStats(userId: String): Stats? {
        val currentDate = LocalDateTime.now() // TODO() take user input for year
        val (startOfWeek, endOfWeek) = getCurrentWeekDates(currentDate)
        val (startOfYear, endOfYear) = getCurrentYearDates(currentDate.year)
        val (startDate, endDate) = getStartAndEndTimestamps(currentDate.year, currentDate.monthValue)

        val query = """[
          {
            ${'$'}match: {
              ${'$'}expr: { 
                ${'$'}eq: [ 
                  '${'$'}userId', { ${'$'}toObjectId: "$userId" } 
                ] 
              } 
            }
          }
          {
            ${'$'}group: {
              _id: null,
              totalIncomeWeekly: {
                ${'$'}sum: {
                  ${'$'}cond: [
                    {
                      ${'$'}and: [
                        {${'$'}eq: ["${'$'}type", "Income"]},
                        {"${'$'}gte": ["${'$'}timestamp", $startOfWeek]},
                        {"${'$'}lte": ["${'$'}timestamp", $endOfWeek]}
                      ]
                    },
                    "${'$'}amount",
                    0
                  ]
                }
              },
              totalIncomeMonthly: {
                ${'$'}sum: {
                  ${'$'}cond: [
                    {
                      ${'$'}and: [
                        {${'$'}eq: ["${'$'}type", "Income"]},
                        {"${'$'}gte": ["${'$'}timestamp", $startDate]},
                        {"${'$'}lte": ["${'$'}timestamp", $endDate]}
                      ]
                    },
                    "${'$'}amount",
                    0
                  ]
                }
              },
              totalIncomeYearly: {
                ${'$'}sum: {
                  ${'$'}cond: [
                    {
                      ${'$'}and: [
                        {${'$'}eq: ["${'$'}type", "Income"]},
                        {"${'$'}gte": ["${'$'}timestamp", $startOfYear]},
                        {"${'$'}lte": ["${'$'}timestamp", $endOfYear]}
                      ]
                    },
                    "${'$'}amount",
                    0
                  ]
                }
              },
              totalExpenseWeekly: {
                ${'$'}sum: {
                  ${'$'}cond: [
                    {
                      ${'$'}and: [
                        {${'$'}eq: ["${'$'}type", "Expense"]},
                        {"${'$'}gte": ["${'$'}timestamp", $startOfWeek]},
                        {"${'$'}lte": ["${'$'}timestamp", $endOfWeek]}
                      ]
                    },
                    "${'$'}amount",
                    0
                  ]
                }
              },
              totalExpenseMonthly: {
                ${'$'}sum: {
                  ${'$'}cond: [
                    {
                      ${'$'}and: [
                        {${'$'}eq: ["${'$'}type", "Expense"]},
                        {"${'$'}gte": ["${'$'}timestamp", $startDate]},
                        {"${'$'}lte": ["${'$'}timestamp", $endDate]}
                      ]
                    },
                    "${'$'}amount",
                    0
                  ]
                }
              },
              totalExpenseYearly: {
                ${'$'}sum: {
                  ${'$'}cond: [
                    {
                      ${'$'}and: [
                        {${'$'}eq: ["${'$'}type", "Expense"]},
                        {"${'$'}gte": ["${'$'}timestamp", $startOfYear]},
                        {"${'$'}lte": ["${'$'}timestamp", $endOfYear]}
                      ]
                    },
                    "${'$'}amount",
                    0
                  ]
                }
              }
            }
          }
        ]""".trimIndent()

        return transactions.aggregate<Stats>(query).first()

    }

    suspend fun postAll(transactionsList: List<Transaction>): Boolean =
        transactions.insertMany(transactionsList).wasAcknowledged()

    suspend fun getPaginatedData(pageNo: Int, size: Int = 2): Pair<List<Transaction>, Int> {
        val result = transactions.aggregate<Test>(
            facet(
                Facet(
                    "count",
                    count()
                ),
                Facet(
                    "transactions", listOf(
                        skip((pageNo - 1) * size),
                        limit(size)
                    )
                )
            )
        ).first()
        val transactionsList = result?.transactions ?: emptyList()
        val count = result?.count ?: 0
        return transactionsList to count.toInt()
    }
}