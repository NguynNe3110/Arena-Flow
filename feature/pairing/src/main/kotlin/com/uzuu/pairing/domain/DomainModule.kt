package com.uzuu.pairing.domain

import com.uzuu.pairing.domain.usecase.ConfirmPackageUseCase
import com.uzuu.pairing.domain.usecase.GetPairingRoomUseCase
import com.uzuu.pairing.domain.usecase.SelectPairingOptionUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val domainModule =
    module {
        singleOf(::GetPairingRoomUseCase)
        singleOf(::SelectPairingOptionUseCase)
        singleOf(::ConfirmPackageUseCase)
    }
