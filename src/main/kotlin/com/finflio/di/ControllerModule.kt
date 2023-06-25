package com.finflio.di

import com.finflio.controllers.AuthController
import com.finflio.controllers.TransactionController
import org.koin.dsl.module

val controllerModule = module {
    single { AuthController(get(), get(), get(), get()) }
    single { TransactionController(get()) }
}