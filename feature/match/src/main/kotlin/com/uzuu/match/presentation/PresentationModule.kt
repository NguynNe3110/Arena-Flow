package com.uzuu.match.presentation

import com.uzuu.match.presentation.screen.matchlist.MatchListViewModel
import com.uzuu.match.presentation.screen.matchdetail.MatchDetailViewModel
import com.uzuu.match.presentation.screen.matchcreate.MatchCreateViewModel
import com.uzuu.match.presentation.screen.matchcreate.MatchDraftHolder
import com.uzuu.match.presentation.screen.matchreview.MatchReviewViewModel
import com.uzuu.match.presentation.screen.pairingroom.PairingRoomViewModel
import com.uzuu.match.presentation.screen.mercenarylist.MercenaryListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModule =
    module {
        single { MatchDraftHolder() }
        // ViewModels
        viewModelOf(::MatchListViewModel)
        viewModelOf(::MatchDetailViewModel)
        viewModelOf(::MatchCreateViewModel)
        viewModelOf(::MatchReviewViewModel)
        viewModelOf(::PairingRoomViewModel)
        viewModelOf(::MercenaryListViewModel)
    }
