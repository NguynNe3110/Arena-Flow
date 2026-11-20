package com.uzuu.match.domain

import com.uzuu.match.domain.usecase.ApplyMercenaryUseCase
import com.uzuu.match.domain.usecase.ConfirmPackageUseCase
import com.uzuu.match.domain.usecase.CreateMatchUseCase
import com.uzuu.match.domain.usecase.GetMatchDetailUseCase
import com.uzuu.match.domain.usecase.GetMatchUseCase
import com.uzuu.match.domain.usecase.GetMercenaryPostsUseCase
import com.uzuu.match.domain.usecase.GetPairingRoomUseCase
import com.uzuu.match.domain.usecase.SelectPairingOptionUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val domainModule =
    module {
        singleOf(::GetMatchUseCase)
        singleOf(::GetMatchDetailUseCase)
        singleOf(::CreateMatchUseCase)
        singleOf(::GetPairingRoomUseCase)
        singleOf(::ConfirmPackageUseCase)
        singleOf(::SelectPairingOptionUseCase)
        singleOf(::GetMercenaryPostsUseCase)
        singleOf(::ApplyMercenaryUseCase)
    }
