package com.igorwojda.showcase.feature.base.data

import com.igorwojda.showcase.feature.base.data.auth.SharedPrefsAuthTokenStore
import com.igorwojda.showcase.feature.base.domain.repository.AuthTokenStore
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val baseDataModule =
    module {
        singleOf(::SharedPrefsAuthTokenStore) { bind<AuthTokenStore>() }
        includes(variantBaseDataModule)
    }
