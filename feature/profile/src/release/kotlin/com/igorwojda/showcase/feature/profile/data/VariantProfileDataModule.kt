package com.igorwojda.showcase.feature.profile.data

import com.igorwojda.showcase.feature.profile.data.datasource.api.service.ProfileRetrofitService
import org.koin.dsl.module
import retrofit2.Retrofit

internal val variantProfileDataModule =
    module {
        single { get<Retrofit>().create(ProfileRetrofitService::class.java) }
    }
