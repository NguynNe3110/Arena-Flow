package com.igorwojda.showcase.feature.match.data

import com.igorwojda.showcase.feature.match.data.repository.MatchRepositoryImpl
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val matchDataModule =
    module {
        includes(variantMatchDataModule)
        singleOf(::MatchRepositoryImpl) { bind<MatchRepository>() }
    }
