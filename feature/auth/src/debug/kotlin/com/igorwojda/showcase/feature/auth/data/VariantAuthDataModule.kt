package com.igorwojda.showcase.feature.auth.data

import com.igorwojda.showcase.feature.auth.data.datasource.api.service.AuthRetrofitService
import com.igorwojda.showcase.feature.auth.data.datasource.api.service.MockAuthRetrofitService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val variantAuthDataModule =
    module {
        singleOf(::MockAuthRetrofitService) { bind<AuthRetrofitService>() }
    }
