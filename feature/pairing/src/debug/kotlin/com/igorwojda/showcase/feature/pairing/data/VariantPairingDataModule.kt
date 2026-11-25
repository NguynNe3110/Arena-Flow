package com.igorwojda.showcase.feature.pairing.data

import com.igorwojda.showcase.feature.pairing.data.datasource.api.MockPairingRetrofitService
import com.igorwojda.showcase.feature.pairing.data.datasource.api.service.PairingRetrofitService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val variantPairingDataModule =
    module {
        singleOf(::MockPairingRetrofitService) { bind<PairingRetrofitService>() }
    }
