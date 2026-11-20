package com.uzuu.mercenary

import com.uzuu.mercenary.data.dataModule
import com.uzuu.mercenary.domain.domainModule
import com.uzuu.mercenary.presentation.presentationModule

val featureMercenaryModules =
    listOf(
        dataModule,
        domainModule,
        presentationModule,
    )
