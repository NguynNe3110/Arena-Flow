package com.igorwojda.showcase.feature.team.data

import com.igorwojda.showcase.feature.team.data.datasource.api.service.TeamMemberRetrofitService
import com.igorwojda.showcase.feature.team.data.datasource.api.service.TeamRetrofitService
import org.koin.dsl.module
import retrofit2.Retrofit

internal val variantTeamDataModule =
    module {
        single<TeamRetrofitService> { get<Retrofit>().create(TeamRetrofitService::class.java) }
        single<TeamMemberRetrofitService> { get<Retrofit>().create(TeamMemberRetrofitService::class.java) }
    }
