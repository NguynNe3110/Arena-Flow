package com.igorwojda.showcase.feature.mercenary.domain

import com.igorwojda.showcase.feature.mercenary.domain.usecase.AcceptMercenaryApplicationUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.ApplyMercenaryUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.CloseMercenaryPostUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.CreateMercenaryPostUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetMercenaryPostDetailUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetMercenaryPostsUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetMyMercenaryApplicationsUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetMyTeamMercenaryPostsUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetPostApplicationsUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.RejectMercenaryApplicationUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.UpdateMercenaryPostUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.WithdrawMercenaryApplicationUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/** Domain Koin module for mercenary use cases. */
internal val mercenaryDomainModule =
    module {
        singleOf(::GetMercenaryPostsUseCase)
        singleOf(::GetMyTeamMercenaryPostsUseCase)
        singleOf(::GetMercenaryPostDetailUseCase)
        singleOf(::CreateMercenaryPostUseCase)
        singleOf(::UpdateMercenaryPostUseCase)
        singleOf(::CloseMercenaryPostUseCase)
        singleOf(::ApplyMercenaryUseCase)
        singleOf(::GetPostApplicationsUseCase)
        singleOf(::AcceptMercenaryApplicationUseCase)
        singleOf(::RejectMercenaryApplicationUseCase)
        singleOf(::WithdrawMercenaryApplicationUseCase)
        singleOf(::GetMyMercenaryApplicationsUseCase)
    }
