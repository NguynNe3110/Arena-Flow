package com.uzuu.match

import com.uzuu.match.data.dataModule
import com.uzuu.match.domain.domainModule
import com.uzuu.match.presentation.presentationModule

val featureMatchModules =
    listOf(
        dataModule,
        domainModule,
        presentationModule,
    )
