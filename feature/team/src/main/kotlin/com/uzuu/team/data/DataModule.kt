package com.uzuu.team.data

import com.uzuu.team.data.datasource.api.MockTeamDataSource
import com.uzuu.team.data.datasource.api.TeamRemoteDataSource
import com.uzuu.team.data.repository.TeamRepositoryImpl
import com.uzuu.team.domain.repository.TeamRepository
import org.koin.dsl.module

internal val dataModule =
    module {
        single<TeamRemoteDataSource> { MockTeamDataSource() }
        single<TeamRepository> { TeamRepositoryImpl(get()) }
    }
