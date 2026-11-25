package com.igorwojda.showcase.feature.mercenary.data

import com.igorwojda.showcase.feature.mercenary.data.datasource.api.service.MercenaryCommandRetrofitService
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.service.MercenaryQueryRetrofitService
import org.koin.dsl.module
import retrofit2.Retrofit

/** Release data module binding real Retrofit mercenary services. */
internal val variantMercenaryDataModule =
    module {
        single { get<Retrofit>().create(MercenaryQueryRetrofitService::class.java) }
        single { get<Retrofit>().create(MercenaryCommandRetrofitService::class.java) }
    }
