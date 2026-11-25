package com.igorwojda.showcase.feature.reputation.data

import com.igorwojda.showcase.feature.reputation.data.datasource.api.service.ReputationCommandRetrofitService
import com.igorwojda.showcase.feature.reputation.data.datasource.api.service.ReputationQueryRetrofitService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val variantReputationDataModule =
    module {
        singleOf(::MockReputationQueryRetrofitService) { bind<ReputationQueryRetrofitService>() }
        singleOf(::MockReputationCommandRetrofitService) { bind<ReputationCommandRetrofitService>() }
    }
