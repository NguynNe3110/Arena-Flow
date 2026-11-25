package com.igorwojda.showcase.feature.reputation.domain

import com.igorwojda.showcase.feature.reputation.domain.usecase.DisputeReportUseCase
import com.igorwojda.showcase.feature.reputation.domain.usecase.GetMatchRatingsUseCase
import com.igorwojda.showcase.feature.reputation.domain.usecase.GetTeamReportsUseCase
import com.igorwojda.showcase.feature.reputation.domain.usecase.GetTeamReputationUseCase
import com.igorwojda.showcase.feature.reputation.domain.usecase.SubmitChatReportUseCase
import com.igorwojda.showcase.feature.reputation.domain.usecase.SubmitMatchRatingUseCase
import com.igorwojda.showcase.feature.reputation.domain.usecase.SubmitMatchReportUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val reputationDomainModule =
    module {
        singleOf(::GetMatchRatingsUseCase)
        singleOf(::SubmitMatchRatingUseCase)
        singleOf(::SubmitMatchReportUseCase)
        singleOf(::SubmitChatReportUseCase)
        singleOf(::GetTeamReportsUseCase)
        singleOf(::DisputeReportUseCase)
        singleOf(::GetTeamReputationUseCase)
    }
