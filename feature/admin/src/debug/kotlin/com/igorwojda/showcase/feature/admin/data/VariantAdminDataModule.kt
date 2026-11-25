package com.igorwojda.showcase.feature.admin.data

import com.igorwojda.showcase.feature.admin.data.datasource.api.service.AdminModerationRetrofitService
import com.igorwojda.showcase.feature.admin.data.datasource.api.service.AdminUserRetrofitService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val variantAdminDataModule =
    module {
        singleOf(::MockAdminStateHolder)
        singleOf(::MockAdminModerationRetrofitService) { bind<AdminModerationRetrofitService>() }
        singleOf(::MockAdminUserRetrofitService) { bind<AdminUserRetrofitService>() }
    }
