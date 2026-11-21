package com.uzuu.team.domain

import com.uzuu.team.domain.usecase.CreateTeamUseCase
import com.uzuu.team.domain.usecase.GetMyTeamUseCase
import com.uzuu.team.domain.usecase.InviteTeamMemberUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val domainModule =
    module {
        singleOf(::GetMyTeamUseCase)
        singleOf(::CreateTeamUseCase)
        singleOf(::InviteTeamMemberUseCase)
    }
