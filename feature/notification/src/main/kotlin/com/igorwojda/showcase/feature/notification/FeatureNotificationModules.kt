package com.igorwojda.showcase.feature.notification

import com.igorwojda.showcase.feature.notification.data.notificationDataModule
import com.igorwojda.showcase.feature.notification.domain.notificationDomainModule
import com.igorwojda.showcase.feature.notification.presentation.notificationPresentationModule

/** Koin modules exported by `:feature:notification`. */
val featureNotificationModules =
    listOf(
        notificationDataModule,
        notificationDomainModule,
        notificationPresentationModule,
    )
