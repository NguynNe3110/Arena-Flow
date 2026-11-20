package com.uzuu.pairing.presentation

import com.uzuu.pairing.presentation.screen.pairingroom.PairingRoomViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModule =
    module {
        viewModelOf(::PairingRoomViewModel)
    }
