package com.igorwojda.showcase.feature.base

import com.igorwojda.showcase.feature.base.data.baseDataModule
import com.igorwojda.showcase.feature.base.domain.baseDomainModule

val featureBaseModules =
    listOf(
        baseDataModule,
        baseDomainModule,
    )
