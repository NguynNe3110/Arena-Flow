package com.igorwojda.showcase.feature.pairing.domain

import com.igorwojda.showcase.feature.pairing.domain.usecase.AddPairingOptionUseCase
import com.igorwojda.showcase.feature.pairing.domain.usecase.CancelPairingUseCase
import com.igorwojda.showcase.feature.pairing.domain.usecase.ConfirmPairingUseCase
import com.igorwojda.showcase.feature.pairing.domain.usecase.DisputePairingUseCase
import com.igorwojda.showcase.feature.pairing.domain.usecase.GetPairingRoomUseCase
import com.igorwojda.showcase.feature.pairing.domain.usecase.SelectPairingOptionUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

internal val pairingDomainModule =
    module {
        factoryOf(::AddPairingOptionUseCase)
        factoryOf(::CancelPairingUseCase)
        factoryOf(::ConfirmPairingUseCase)
        factoryOf(::DisputePairingUseCase)
        factoryOf(::GetPairingRoomUseCase)
        factoryOf(::SelectPairingOptionUseCase)
    }
