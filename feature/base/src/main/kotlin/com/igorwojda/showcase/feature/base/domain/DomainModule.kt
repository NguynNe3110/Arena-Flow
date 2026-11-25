package com.igorwojda.showcase.feature.base.domain

import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.base.domain.usecase.ManageDemoControlsUseCase
import com.igorwojda.showcase.feature.base.domain.usecase.UpdateActingTeamUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val baseDomainModule =
    module {
        singleOf(::GetSessionStateUseCase)
        singleOf(::UpdateActingTeamUseCase)
        singleOf(::ManageDemoControlsUseCase)
    }
