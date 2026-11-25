package com.igorwojda.showcase.feature.notification.domain

import com.igorwojda.showcase.feature.notification.domain.usecase.GetNotificationsUseCase
import com.igorwojda.showcase.feature.notification.domain.usecase.GetUnreadNotificationCountUseCase
import com.igorwojda.showcase.feature.notification.domain.usecase.MarkAllNotificationsReadUseCase
import com.igorwojda.showcase.feature.notification.domain.usecase.MarkNotificationReadUseCase
import com.igorwojda.showcase.feature.notification.domain.usecase.RegisterFcmTokenUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val notificationDomainModule =
    module {
        singleOf(::GetNotificationsUseCase)
        singleOf(::GetUnreadNotificationCountUseCase)
        singleOf(::MarkNotificationReadUseCase)
        singleOf(::MarkAllNotificationsReadUseCase)
        singleOf(::RegisterFcmTokenUseCase)
    }
