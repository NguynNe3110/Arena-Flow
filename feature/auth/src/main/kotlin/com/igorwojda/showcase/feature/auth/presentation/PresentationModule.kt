package com.igorwojda.showcase.feature.auth.presentation

import com.igorwojda.showcase.feature.auth.presentation.screen.login.LoginMethodViewModel
import com.igorwojda.showcase.feature.auth.presentation.screen.onboarding.OnboardingViewModel
import com.igorwojda.showcase.feature.auth.presentation.screen.otp.OtpVerifyViewModel
import com.igorwojda.showcase.feature.auth.presentation.screen.phone.PhoneInputViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModule =
    module {
        viewModelOf(::OnboardingViewModel)
        viewModelOf(::LoginMethodViewModel)
        viewModelOf(::PhoneInputViewModel)
        viewModelOf(::OtpVerifyViewModel)
    }
