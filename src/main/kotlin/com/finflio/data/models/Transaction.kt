package com.finflio.data.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Transaction(
    val timestamp: Long,
    val type: String, // should be one of these values only ["Expense", "Income", "Unsettled Transaction"]
    val category: String,
    val paymentMethod: String,
    val description: String,
    val amount: Float, // must be greater than 0
    val attachment: String? = null, // must be a cloudinary url
    val from: String? = null, // if to field is filled then from should be blank or null
    val to: String? = null, // if from field is filled then to should be blank or null
    @Contextual @BsonId
    val id: ObjectId = ObjectId(),
    @Contextual @BsonId val userId: ObjectId,
)