package com.uzuu.pairing

import com.uzuu.pairing.data.dataModule
import com.uzuu.pairing.domain.domainModule
import com.uzuu.pairing.presentation.presentationModule

val featurePairingModules =
    listOf(
        dataModule,
        domainModule,
        presentationModule,
    )
