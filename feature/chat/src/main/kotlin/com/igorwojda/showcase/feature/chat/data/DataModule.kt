package com.igorwojda.showcase.feature.chat.data

import com.igorwojda.showcase.feature.chat.data.repository.ChatRepositoryImpl
import com.igorwojda.showcase.feature.chat.domain.repository.ChatRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val chatDataModule =
    module {
        includes(variantChatDataModule)
        singleOf(::ChatRepositoryImpl) { bind<ChatRepository>() }
    }
