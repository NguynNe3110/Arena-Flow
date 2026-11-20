package com.uzuu.mercenary.domain

import com.uzuu.mercenary.domain.usecase.ApplyMercenaryUseCase
import com.uzuu.mercenary.domain.usecase.GetMercenaryPostsUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val domainModule =
    module {
        singleOf(::GetMercenaryPostsUseCase)
        singleOf(::ApplyMercenaryUseCase)
    }
