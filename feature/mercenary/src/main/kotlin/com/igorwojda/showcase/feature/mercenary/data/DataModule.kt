package com.igorwojda.showcase.feature.mercenary.data

import com.igorwojda.showcase.feature.mercenary.data.repository.MercenaryRepositoryImpl
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/** Data Koin module for the mercenary feature. */
internal val mercenaryDataModule =
    module {
        includes(variantMercenaryDataModule)
        singleOf(::MercenaryRepositoryImpl) { bind<MercenaryRepository>() }
    }
