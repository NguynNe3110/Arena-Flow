package com.igorwojda.showcase.feature.notification.data

import com.igorwojda.showcase.feature.notification.data.repository.NotificationRepositoryImpl
import com.igorwojda.showcase.feature.notification.domain.repository.NotificationRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val notificationDataModule =
    module {
        includes(variantNotificationDataModule)
        singleOf(::NotificationRepositoryImpl) { bind<NotificationRepository>() }
    }
