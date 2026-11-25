package com.igorwojda.showcase.feature.match.presentation

import com.igorwojda.showcase.feature.match.presentation.screen.matchcreate.MatchCreateViewModel
import com.igorwojda.showcase.feature.match.presentation.screen.matchcreate.MatchDraftHolder
import com.igorwojda.showcase.feature.match.presentation.screen.matchdetail.MatchDetailViewModel
import com.igorwojda.showcase.feature.match.presentation.screen.matchfeed.MatchFeedViewModel
import com.igorwojda.showcase.feature.match.presentation.screen.matchreview.MatchReviewViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val matchPresentationModule =
    module {
        singleOf(::MatchDraftHolder)
        viewModelOf(::MatchFeedViewModel)
        viewModelOf(::MatchDetailViewModel)
        viewModelOf(::MatchCreateViewModel)
        viewModelOf(::MatchReviewViewModel)
    }
