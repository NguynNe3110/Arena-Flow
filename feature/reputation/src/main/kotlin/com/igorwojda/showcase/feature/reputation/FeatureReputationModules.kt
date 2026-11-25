package com.igorwojda.showcase.feature.reputation

import com.igorwojda.showcase.feature.reputation.data.reputationDataModule
import com.igorwojda.showcase.feature.reputation.domain.reputationDomainModule
import com.igorwojda.showcase.feature.reputation.presentation.reputationPresentationModule

val featureReputationModules =
    listOf(
        reputationDataModule,
        reputationDomainModule,
        reputationPresentationModule,
    )
