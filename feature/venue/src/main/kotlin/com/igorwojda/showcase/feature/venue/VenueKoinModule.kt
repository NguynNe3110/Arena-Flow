package com.igorwojda.showcase.feature.venue

import com.igorwojda.showcase.feature.venue.data.dataModule
import com.igorwojda.showcase.feature.venue.domain.domainModule
import com.igorwojda.showcase.feature.venue.presentation.presentationModule

val featureVenueModules = listOf(presentationModule, domainModule, dataModule)
