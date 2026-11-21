package com.uzuu.team

import com.uzuu.team.data.dataModule
import com.uzuu.team.domain.domainModule
import com.uzuu.team.presentation.presentationModule

val featureTeamModules =
    listOf(
        dataModule,
        domainModule,
        presentationModule,
    )
