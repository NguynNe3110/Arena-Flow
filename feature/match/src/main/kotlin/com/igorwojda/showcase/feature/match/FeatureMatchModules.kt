package com.igorwojda.showcase.feature.match

import com.igorwojda.showcase.feature.match.data.matchDataModule
import com.igorwojda.showcase.feature.match.domain.matchDomainModule
import com.igorwojda.showcase.feature.match.presentation.matchPresentationModule

/** Koin modules exported by :feature:match. */
val featureMatchModules =
    listOf(
        matchDataModule,
        matchDomainModule,
        matchPresentationModule,
    )
