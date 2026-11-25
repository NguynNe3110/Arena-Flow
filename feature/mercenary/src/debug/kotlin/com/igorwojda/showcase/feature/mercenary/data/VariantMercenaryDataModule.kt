package com.igorwojda.showcase.feature.mercenary.data

import com.igorwojda.showcase.feature.mercenary.data.datasource.api.service.MercenaryCommandRetrofitService
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.service.MercenaryQueryRetrofitService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/** Debug data module binding mock mercenary services backed by ArenaInMemoryStore. */
internal val variantMercenaryDataModule =
    module {
        singleOf(::MockMercenaryQueryRetrofitService) { bind<MercenaryQueryRetrofitService>() }
        singleOf(::MockMercenaryCommandRetrofitService) { bind<MercenaryCommandRetrofitService>() }
    }
