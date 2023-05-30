package com.finflio.di

import com.mongodb.ConnectionString
import org.koin.dsl.module
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val dbModule = module {
    single { provideMongoInstance() }
}

private fun provideMongoInstance(): CoroutineDatabase {
    val mongoURI = System.getenv("MONGO_URI")
    val db = System.getenv("DB_NAME")
    val client = KMongo.createClient(ConnectionString(mongoURI)).coroutine
    return client.getDatabase(db)
}