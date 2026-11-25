package com.igorwojda.showcase.feature.match.data

import com.igorwojda.showcase.feature.match.data.datasource.api.service.MatchCommandRetrofitService
import com.igorwojda.showcase.feature.match.data.datasource.api.service.MatchQueryRetrofitService
import com.igorwojda.showcase.feature.match.data.datasource.api.service.MatchRequestRetrofitService
import org.koin.dsl.module
import retrofit2.Retrofit

internal val variantMatchDataModule =
    module {
        single { get<Retrofit>().create(MatchQueryRetrofitService::class.java) }
        single { get<Retrofit>().create(MatchCommandRetrofitService::class.java) }
        single { get<Retrofit>().create(MatchRequestRetrofitService::class.java) }
    }
