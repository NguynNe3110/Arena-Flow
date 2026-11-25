package com.igorwojda.showcase.feature.reputation.presentation

import com.igorwojda.showcase.feature.reputation.presentation.screen.postmatchreport.PostMatchReportViewModel
import com.igorwojda.showcase.feature.reputation.presentation.screen.reputationhistory.ReputationHistoryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val reputationPresentationModule =
    module {
        viewModelOf(::PostMatchReportViewModel)
        viewModelOf(::ReputationHistoryViewModel)
    }
