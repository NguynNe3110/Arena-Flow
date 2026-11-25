package com.igorwojda.showcase.feature.chat.presentation

import com.igorwojda.showcase.feature.chat.presentation.screen.inbox.ChatInboxViewModel
import com.igorwojda.showcase.feature.chat.presentation.screen.room.ChatRoomViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val chatPresentationModule =
    module {
        viewModelOf(::ChatInboxViewModel)
        viewModelOf(::ChatRoomViewModel)
    }
