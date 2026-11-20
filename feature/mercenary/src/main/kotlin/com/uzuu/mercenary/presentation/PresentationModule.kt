package com.uzuu.mercenary.presentation

import com.uzuu.mercenary.presentation.screen.mercenarylist.MercenaryListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModule =
    module {
        viewModelOf(::MercenaryListViewModel)
    }
