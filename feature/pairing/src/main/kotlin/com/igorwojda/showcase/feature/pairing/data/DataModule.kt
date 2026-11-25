package com.igorwojda.showcase.feature.pairing.data

import com.igorwojda.showcase.feature.pairing.data.repository.PairingRepositoryImpl
import com.igorwojda.showcase.feature.pairing.domain.repository.PairingRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val pairingDataModule =
    module {
        includes(variantPairingDataModule)
        singleOf(::PairingRepositoryImpl) { bind<PairingRepository>() }
    }
