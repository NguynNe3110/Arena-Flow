package com.igorwojda.showcase.feature.profile.domain

import com.igorwojda.showcase.feature.profile.domain.usecase.DeleteAccountUseCase
import com.igorwojda.showcase.feature.profile.domain.usecase.GetCaptainedTeamsForDeleteUseCase
import com.igorwojda.showcase.feature.profile.domain.usecase.GetMyProfileUseCase
import com.igorwojda.showcase.feature.profile.domain.usecase.GetProvincesUseCase
import com.igorwojda.showcase.feature.profile.domain.usecase.GetWardsByProvinceUseCase
import com.igorwojda.showcase.feature.profile.domain.usecase.UpdateMyProfileUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val domainModule =
    module {
        singleOf(::GetMyProfileUseCase)
        singleOf(::UpdateMyProfileUseCase)
        singleOf(::DeleteAccountUseCase)
        singleOf(::GetProvincesUseCase)
        singleOf(::GetWardsByProvinceUseCase)
        singleOf(::GetCaptainedTeamsForDeleteUseCase)
    }
