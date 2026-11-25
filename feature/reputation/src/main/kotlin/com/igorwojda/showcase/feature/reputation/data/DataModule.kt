package com.igorwojda.showcase.feature.reputation.data

import com.igorwojda.showcase.feature.reputation.data.repository.ReputationRepositoryImpl
import com.igorwojda.showcase.feature.reputation.domain.repository.ReputationRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val reputationDataModule =
    module {
        includes(variantReputationDataModule)
        singleOf(::ReputationRepositoryImpl) { bind<ReputationRepository>() }
    }
