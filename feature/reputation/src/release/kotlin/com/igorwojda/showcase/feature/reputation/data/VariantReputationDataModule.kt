package com.igorwojda.showcase.feature.reputation.data

import com.igorwojda.showcase.feature.reputation.data.datasource.api.service.ReputationCommandRetrofitService
import com.igorwojda.showcase.feature.reputation.data.datasource.api.service.ReputationQueryRetrofitService
import org.koin.dsl.module
import retrofit2.Retrofit

internal val variantReputationDataModule =
    module {
        single { get<Retrofit>().create(ReputationQueryRetrofitService::class.java) }
        single { get<Retrofit>().create(ReputationCommandRetrofitService::class.java) }
    }
