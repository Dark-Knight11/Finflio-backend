package com.finflio.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Transaction(
    val timestamp: String,
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

object ObjectIDSerializer : KSerializer<ObjectId> {

    override fun deserialize(decoder: Decoder): ObjectId =
        ObjectId(decoder.decodeString())


    override fun serialize(encoder: Encoder, value: ObjectId) {
        encoder.encodeString(value.toString())
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ObjectID", PrimitiveKind.STRING)
}