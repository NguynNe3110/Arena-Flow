package com.igorwojda.showcase.feature.admin.data

import com.igorwojda.showcase.feature.admin.data.datasource.api.service.AdminModerationRetrofitService
import com.igorwojda.showcase.feature.admin.data.datasource.api.service.AdminUserRetrofitService
import org.koin.dsl.module
import retrofit2.Retrofit

internal val variantAdminDataModule =
    module {
        single { get<Retrofit>().create(AdminModerationRetrofitService::class.java) }
        single { get<Retrofit>().create(AdminUserRetrofitService::class.java) }
    }
