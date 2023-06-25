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

    suspend fun getUnsettledTransactions(
        userId: String,
        pageNo: Int,
        size: Int = 10
    ): Pair<List<Transaction>, Int> {
        val count = transactions.aggregate<Transaction>(
            match(Transaction::type eq "Unsettled"),
            match(Transaction::userId eq ObjectId(userId)),
        ).toList().size

        val totalPages = ceil(count.fdiv(size)).toInt()

        return transactions.aggregate<Transaction>(
            match(Transaction::type eq "Unsettled"),
            match(Transaction::userId eq ObjectId(userId)),
            sort(descending(Transaction::timestamp)),
            skip((pageNo - 1) * size),
            limit(size)
        ).toList() to totalPages
    }


    suspend fun getFilteredTransaction(
        month: Month,
        userId: String,
        pageNo: Int,
        size: Int = 10
    ): Triple<List<Transaction>, Int, Int> {
        val currentYear = LocalDateTime.now().year // TODO() take user input for year
        val (startDate, endDate) = getStartAndEndTimestamps(currentYear, month.value)

        val query = """[
          {
            ${'$'}match: {
              ${'$'}and: [
                {
                  ${'$'}expr: {
                    ${'$'}eq: [
                      "${'$'}userId",
                      {
                        ${'$'}toObjectId: "$userId",
                      },
                    ],
                  },
                },
                {
                  ${'$'}expr: {
                    ${'$'}ne: [
                      "${'$'}type", "Unsettled"
                    ],
                  },
                },
                {
                  timestamp: {
                    ${'$'}gte: $startDate,
                    ${'$'}lte: $endDate,
                  },
                },
              ],
            },
          },
          {
            ${'$'}group: {
              _id: null,
              monthTotal: {
                ${'$'}sum: {
                  ${'$'}cond: [
                    { 
                      ${'$'}eq: [ "${'$'}type", "Expense" ]
                    },
                    "${'$'}amount",
                    0,
                  ],
                },
              },
              total: {
                ${'$'}count: {},
              },
            }
          }
        ]""".trimIndent()

        val totalList = transactions.aggregate<TotalList>(query).first()

        val count = totalList?.total ?: 0
        val monthTotal = totalList?.monthTotal ?: 0

        val totalPages = ceil(count.fdiv(size)).toInt()

        return Triple(
            transactions.aggregate<Transaction>(
                match(
                    Transaction::userId eq ObjectId(userId),
                    Transaction::type ne "Unsettled"
                ),
                match(
                    Transaction::timestamp gte startDate,
                    Transaction::timestamp lte endDate
                ),
                sort(descending(Transaction::timestamp)),
                skip((pageNo - 1) * size),
                limit(size)
            ).toList(), totalPages, monthTotal
        )
    }

    /**
     * Retrieves the statistical data (weekly, monthly, yearly) for a given user.
     * @param userId The unique identifier of the user.
     * @return The statistical data for the user, or null if no data is available.
     */
    suspend fun getStats(userId: String): Stats? {
        val currentDate = LocalDateTime.now() // TODO() take user input for year
        val (startOfWeek, endOfWeek) = getCurrentWeekDates(currentDate)
        val (startOfYear, endOfYear) = getCurrentYearDates(currentDate.year)
        val (startDate, endDate) = getStartAndEndTimestamps(currentDate.year, currentDate.monthValue)


        /**
         * This query is written in the MongoDB Aggregation Framework syntax and is used to retrieve statistical data (weekly, monthly, yearly) for a given user.
         *
         * The query starts with a `$match` stage, which filters the documents based on the `userId` field. It uses the `$expr` operator to compare the `userId` field with the provided `userId` value.
         *
         * The main part of the query is the `$facet` stage, which allows multiple pipelines to be executed within a single aggregation stage. It defines three facets: `weeklyData`, `monthlyData`, and `yearlyData`.
         *
         * For each facet, there are several stages that perform specific operations on the data:
         *
         * 1. The `$match` stage filters the documents based on the type (`Expense` or `Income`) and the timestamp range (`startOfWeek` and `endOfWeek` for `weeklyData`, `startDate` and `endDate` for `monthlyData`, and `startOfYear` and `endOfYear` for `yearlyData`).
         * 2. The `$group` stage groups the documents by the formatted date (`_id`) using the `$dateToString` operator. It also calculates the sum of the daily income and expense based on the type.
         * 3. The `$project` stage reshapes the output by excluding the `_id` field and including the `date`, `totalDailyIncome`, and `totalDailyExpense` fields.
         * 4. The `$sort` stage sorts the documents by the `date` field in ascending order.
         *
         * The result of the query is an array containing three sets of statistical data: `weeklyData`, `monthlyData`, and `yearlyData`. Each set includes an array of documents with the fields `date`, `totalDailyIncome`, and `totalDailyExpense`.
         *
         * The query uses various MongoDB aggregation operators and functions, such as `$match`, `$facet`, `$group`, `$project`, `$sort`, `$expr`, `$eq`, `$in`, `$gte`, `$lte`, `$cond`, `$sum`, `$toDate`, and `$dateToString`, to perform filtering, grouping, projection, sorting, and date manipulation operations.
         */
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
            ${'$'}facet: {
                weeklyData: [
                  {
                    ${'$'}match: {
                      ${'$'}and: [
                        {
                          type: {
                            ${'$'}in: ["Expense", "Income"],
                          },
                        },
                        {
                          timestamp: {
                            ${'$'}gte: $startOfWeek,
                            ${'$'}lte: $endOfWeek,
                          },
                        },
                      ],
                    },
                  },
                  {
                    ${'$'}group: {
                      _id: {
                        ${'$'}dateToString: {
                          format: "%Y-%m-%d",
                          date: {
                            ${'$'}toDate: "${'$'}timestamp",
                          },
                        },
                      },
                      totalDailyIncome: {
                        ${'$'}sum: {
                          ${'$'}cond: [
                            {
                              ${'$'}eq: ["${'$'}type", "Income"],
                            },
                            "${'$'}amount",
                            0,
                          ],
                        },
                      },
                      totalDailyExpense: {
                        ${'$'}sum: {
                          ${'$'}cond: [
                            {
                              ${'$'}eq: ["${'$'}type", "Expense"],
                            },
                            "${'$'}amount",
                            0,
                          ],
                        },
                      },
                    },
                  },
                  {
                    ${'$'}project: {
                      _id: 0,
                      date: "${'$'}_id",
                      totalDailyIncome: 1,
                      totalDailyExpense: 1,
                    },
                  },
                  {
                    ${'$'}sort: {
                      date: 1,
                    },
                  },
                ],
                monthlyData: [
                  {
                    ${'$'}match: {
                      ${'$'}and: [
                        {
                          type: {
                            ${'$'}in: ["Expense", "Income"],
                          },
                        },
                        {
                          timestamp: {
                            ${'$'}gte: $startDate,
                            ${'$'}lte: $endDate,
                          },
                        },
                      ],
                    },
                  },
                  {
                    ${'$'}group: {
                      _id: {
                        ${'$'}dateToString: {
                          format: "%Y-%m-%d",
                          date: {
                            ${'$'}toDate: "${'$'}timestamp",
                          },
                        },
                      },
                      totalDailyIncome: {
                        ${'$'}sum: {
                          ${'$'}cond: [
                            {
                              ${'$'}eq: ["${'$'}type", "Income"],
                            },
                            "${'$'}amount",
                            0,
                          ],
                        },
                      },
                      totalDailyExpense: {
                        ${'$'}sum: {
                          ${'$'}cond: [
                            {
                              ${'$'}eq: ["${'$'}type", "Expense"],
                            },
                            "${'$'}amount",
                            0,
                          ],
                        },
                      },
                    },
                  },
                  {
                    ${'$'}project: {
                      _id: 0,
                      date: "${'$'}_id",
                      totalDailyIncome: 1,
                      totalDailyExpense: 1,
                    },
                  },
                  {
                    ${'$'}sort: {
                      date: 1,
                    },
                  },
                ],
                yearlyData: [
                  {
                    ${'$'}match: {
                      ${'$'}and: [
                        {
                          type: {
                            ${'$'}in: ["Expense", "Income"],
                          },
                        },
                        {
                          timestamp: {
                            ${'$'}gte: $startOfYear,
                            ${'$'}lte: $endOfYear,
                          },
                        },
                      ],
                    },
                  },
                  {
                    ${'$'}group: {
                      _id: {
                        ${'$'}dateToString: {
                          format: "%Y-%m",
                          date: {
                            ${'$'}toDate: "${'$'}timestamp",
                          },
                        },
                      },
                      totalDailyIncome: {
                        ${'$'}sum: {
                          ${'$'}cond: [
                            {
                              ${'$'}eq: ["${'$'}type", "Income"],
                            },
                            "${'$'}amount",
                            0,
                          ],
                        },
                      },
                      totalDailyExpense: {
                        ${'$'}sum: {
                          ${'$'}cond: [
                            {
                              ${'$'}eq: ["${'$'}type", "Expense"],
                            },
                            "${'$'}amount",
                            0,
                          ],
                        },
                      },
                    },
                  },
                  {
                    ${'$'}project: {
                      _id: 0,
                      date: "${'$'}_id",
                      totalDailyIncome: 1,
                      totalDailyExpense: 1,
                    },
                  },
                  {
                    ${'$'}sort: {
                      date: 1,
                    },
                  },
                ],
              },
          }
        ]""".trimIndent()

        val data = transactions.aggregate<Stats>(query).first()

        return getMissingData(
            startOfWeek,
            endOfWeek,
            startDate,
            endDate,
            startOfYear,
            endOfYear,
            data
        )
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

data class TotalList(
    val monthTotal: Int,
    val total: Int? = 0
)