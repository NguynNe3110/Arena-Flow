package com.igorwojda.showcase.feature.match.domain

import com.igorwojda.showcase.feature.match.domain.usecase.AcceptMatchRequestUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.CancelMatchUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.CreateMatchUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.ForfeitMatchUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.GetMatchDetailUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.GetMatchRequestsUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.GetMatchesUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.GetMyMatchesUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.GetSportsUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.RejectMatchRequestUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.SendMatchRequestUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.UpdateCostAgreementUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.UpdateMatchUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.WithdrawMatchRequestUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

internal val matchDomainModule =
    module {
        factoryOf(::GetMatchesUseCase)
        factoryOf(::GetMyMatchesUseCase)
        factoryOf(::GetMatchDetailUseCase)
        factoryOf(::CreateMatchUseCase)
        factoryOf(::UpdateMatchUseCase)
        factoryOf(::UpdateCostAgreementUseCase)
        factoryOf(::CancelMatchUseCase)
        factoryOf(::ForfeitMatchUseCase)
        factoryOf(::GetMatchRequestsUseCase)
        factoryOf(::SendMatchRequestUseCase)
        factoryOf(::AcceptMatchRequestUseCase)
        factoryOf(::RejectMatchRequestUseCase)
        factoryOf(::WithdrawMatchRequestUseCase)
        factoryOf(::GetSportsUseCase)
    }
