package com.igorwojda.showcase.feature.mercenary.presentation

import com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarycreate.MercenaryCreateViewModel
import com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarydetail.MercenaryDetailViewModel
import com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarylist.MercenaryListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Presentation Koin module for mercenary screens and ViewModels. */
internal val mercenaryPresentationModule =
    module {
        viewModelOf(::MercenaryListViewModel)
        viewModelOf(::MercenaryDetailViewModel)
        viewModelOf(::MercenaryCreateViewModel)
    }
