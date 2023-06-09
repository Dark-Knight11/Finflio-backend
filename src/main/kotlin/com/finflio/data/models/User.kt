package com.finflio.data.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    val email: String,
    val password: String,
    val salt: String,
    val name: String,
    @BsonId val id: ObjectId = ObjectId()
)