package com.igorwojda.showcase.feature.auth.data

import com.igorwojda.showcase.feature.auth.data.repository.AuthRepositoryImpl
import com.igorwojda.showcase.feature.auth.domain.repository.AuthRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val dataModule =
    module {
        includes(variantAuthDataModule)
        singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    }
