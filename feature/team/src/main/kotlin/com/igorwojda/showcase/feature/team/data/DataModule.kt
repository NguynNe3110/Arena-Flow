package com.igorwojda.showcase.feature.team.data

import com.igorwojda.showcase.feature.team.data.repository.TeamMemberRepositoryImpl
import com.igorwojda.showcase.feature.team.data.repository.TeamRepositoryImpl
import com.igorwojda.showcase.feature.team.domain.repository.TeamMemberRepository
import com.igorwojda.showcase.feature.team.domain.repository.TeamRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val dataModule =
    module {
        includes(variantTeamDataModule)
        singleOf(::TeamRepositoryImpl) { bind<TeamRepository>() }
        singleOf(::TeamMemberRepositoryImpl) { bind<TeamMemberRepository>() }
    }
