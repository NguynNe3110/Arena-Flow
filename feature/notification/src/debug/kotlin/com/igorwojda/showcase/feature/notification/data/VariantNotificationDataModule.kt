package com.igorwojda.showcase.feature.notification.data

import com.igorwojda.showcase.feature.notification.data.datasource.api.service.NotificationRetrofitService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val variantNotificationDataModule =
    module {
        singleOf(::MockNotificationRetrofitService) { bind<NotificationRetrofitService>() }
    }
