package com.uzuu.match.presentation

import com.uzuu.match.presentation.screen.matchlist.MatchListViewModel
import com.uzuu.match.presentation.screen.matchdetail.MatchDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModule =
    module {
    // ViewModels
    viewModelOf(::MatchListViewModel)
    viewModelOf(::MatchDetailViewModel)
}
