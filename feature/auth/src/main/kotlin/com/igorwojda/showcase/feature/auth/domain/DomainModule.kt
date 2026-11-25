package com.igorwojda.showcase.feature.auth.domain

import com.igorwojda.showcase.feature.auth.domain.usecase.CompleteOnboardingUseCase
import com.igorwojda.showcase.feature.auth.domain.usecase.SendOtpUseCase
import com.igorwojda.showcase.feature.auth.domain.usecase.SignInWithOAuthUseCase
import com.igorwojda.showcase.feature.auth.domain.usecase.VerifyOtpUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val domainModule =
    module {
        singleOf(::CompleteOnboardingUseCase)
        singleOf(::SendOtpUseCase)
        singleOf(::SignInWithOAuthUseCase)
        singleOf(::VerifyOtpUseCase)
    }
