package com.igorwojda.showcase.feature.pairing.data

import com.igorwojda.showcase.feature.pairing.data.datasource.api.service.PairingRetrofitService
import org.koin.dsl.module
import retrofit2.Retrofit

internal val variantPairingDataModule =
    module {
        single<PairingRetrofitService> { get<Retrofit>().create(PairingRetrofitService::class.java) }
    }
