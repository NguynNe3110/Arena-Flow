package com.igorwojda.showcase.feature.pairing

import com.igorwojda.showcase.feature.pairing.data.pairingDataModule
import com.igorwojda.showcase.feature.pairing.domain.pairingDomainModule
import com.igorwojda.showcase.feature.pairing.presentation.pairingPresentationModule

/** Koin modules exported by the `:feature:pairing` feature module. */
val featurePairingModules =
    listOf(
        pairingDataModule,
        pairingDomainModule,
        pairingPresentationModule,
    )
