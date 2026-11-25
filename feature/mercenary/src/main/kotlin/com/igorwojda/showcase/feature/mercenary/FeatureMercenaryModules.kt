package com.igorwojda.showcase.feature.mercenary

import com.igorwojda.showcase.feature.mercenary.data.mercenaryDataModule
import com.igorwojda.showcase.feature.mercenary.domain.mercenaryDomainModule
import com.igorwojda.showcase.feature.mercenary.presentation.mercenaryPresentationModule

/** Koin modules exported by the mercenary feature. */
val featureMercenaryModules =
    listOf(
        mercenaryDataModule,
        mercenaryDomainModule,
        mercenaryPresentationModule,
    )
