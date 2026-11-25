package com.igorwojda.showcase.feature.notification.data

import com.igorwojda.showcase.feature.notification.data.datasource.api.service.NotificationRetrofitService
import org.koin.dsl.module
import retrofit2.Retrofit

internal val variantNotificationDataModule =
    module {
        single { get<Retrofit>().create(NotificationRetrofitService::class.java) }
    }
