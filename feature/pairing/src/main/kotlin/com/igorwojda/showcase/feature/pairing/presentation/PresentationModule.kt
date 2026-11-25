package com.igorwojda.showcase.feature.pairing.presentation

import com.igorwojda.showcase.feature.pairing.presentation.screen.pairingroom.PairingRoomViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val pairingPresentationModule =
    module {
        viewModelOf(::PairingRoomViewModel)
    }
