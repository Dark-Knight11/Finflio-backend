package com.finflio.di

import com.finflio.repository.AuthRepository
import com.finflio.repository.TransactionRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { AuthRepository(get()) }
    single { TransactionRepository(get()) }
}