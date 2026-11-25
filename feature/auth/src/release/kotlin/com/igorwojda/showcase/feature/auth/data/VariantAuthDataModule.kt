package com.igorwojda.showcase.feature.auth.data

import com.igorwojda.showcase.feature.auth.data.datasource.api.service.AuthRetrofitService
import org.koin.dsl.module
import retrofit2.Retrofit

internal val variantAuthDataModule =
    module {
        single<AuthRetrofitService> { get<Retrofit>().create(AuthRetrofitService::class.java) }
    }
