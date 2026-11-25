package com.igorwojda.showcase.feature.chat

import com.igorwojda.showcase.feature.chat.data.chatDataModule
import com.igorwojda.showcase.feature.chat.domain.chatDomainModule
import com.igorwojda.showcase.feature.chat.presentation.chatPresentationModule

/** Koin modules exported by `:feature:chat`. */
val featureChatModules =
    listOf(
        chatDataModule,
        chatDomainModule,
        chatPresentationModule,
    )
