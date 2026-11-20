package com.uzuu.match.domain

import com.uzuu.match.domain.usecase.CreateMatchUseCase
import com.uzuu.match.domain.usecase.GetMatchDetailUseCase
import com.uzuu.match.domain.usecase.GetMatchUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val domainModule =
    module {
        singleOf(::GetMatchUseCase)
        singleOf(::GetMatchDetailUseCase)
        singleOf(::CreateMatchUseCase)
    }
