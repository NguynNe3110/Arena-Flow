package com.igorwojda.showcase.feature.team.data

import com.igorwojda.showcase.feature.team.data.datasource.api.service.MockTeamMemberRetrofitService
import com.igorwojda.showcase.feature.team.data.datasource.api.service.MockTeamRetrofitService
import com.igorwojda.showcase.feature.team.data.datasource.api.service.TeamMemberRetrofitService
import com.igorwojda.showcase.feature.team.data.datasource.api.service.TeamRetrofitService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val variantTeamDataModule =
    module {
        singleOf(::MockTeamRetrofitService) { bind<TeamRetrofitService>() }
        singleOf(::MockTeamMemberRetrofitService) { bind<TeamMemberRetrofitService>() }
    }
