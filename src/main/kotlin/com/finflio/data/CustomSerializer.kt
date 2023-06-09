package com.finflio.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.types.ObjectId

object ObjectIDSerializer : KSerializer<ObjectId> {

    override fun deserialize(decoder: Decoder): ObjectId =
        ObjectId(decoder.decodeString())


    override fun serialize(encoder: Encoder, value: ObjectId) =
        encoder.encodeString(value.toString())


    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ObjectID", PrimitiveKind.STRING)
}