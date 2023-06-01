package com.finflio.di

import com.finflio.controllers.AuthController
import com.finflio.controllers.TransactionController
import com.finflio.security.TokenConfig
import org.koin.dsl.module

val controllerModule = module {
    single { (tokenConfig: TokenConfig) -> AuthController(get(), get(), get(), tokenConfig) }
    single { TransactionController(get()) }
}