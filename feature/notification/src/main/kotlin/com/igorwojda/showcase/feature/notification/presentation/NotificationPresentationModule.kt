package com.igorwojda.showcase.feature.notification.presentation

import com.igorwojda.showcase.feature.notification.presentation.screen.notificationlist.NotificationListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val notificationPresentationModule =
    module {
        viewModelOf(::NotificationListViewModel)
    }
