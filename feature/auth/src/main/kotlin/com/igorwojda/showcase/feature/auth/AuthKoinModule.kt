package com.igorwojda.showcase.feature.auth

import com.igorwojda.showcase.feature.auth.data.dataModule
import com.igorwojda.showcase.feature.auth.domain.domainModule
import com.igorwojda.showcase.feature.auth.presentation.presentationModule

val featureAuthModules =
    listOf(
        presentationModule,
        domainModule,
        dataModule,
    )
