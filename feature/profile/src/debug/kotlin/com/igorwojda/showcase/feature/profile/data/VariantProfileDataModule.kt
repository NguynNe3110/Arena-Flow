package com.igorwojda.showcase.feature.profile.data

import com.igorwojda.showcase.feature.profile.data.datasource.api.service.ProfileRetrofitService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val variantProfileDataModule =
    module {
        singleOf(::MockProfileRetrofitService) { bind<ProfileRetrofitService>() }
    }
