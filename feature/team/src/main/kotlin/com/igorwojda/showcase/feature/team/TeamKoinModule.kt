package com.igorwojda.showcase.feature.team

import com.igorwojda.showcase.feature.team.data.dataModule
import com.igorwojda.showcase.feature.team.domain.domainModule
import com.igorwojda.showcase.feature.team.presentation.presentationModule

val featureTeamModules = listOf(presentationModule, domainModule, dataModule)
