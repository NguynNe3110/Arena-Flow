package com.igorwojda.showcase.feature.chat.data

import com.igorwojda.showcase.feature.chat.data.datasource.api.service.ChatRetrofitService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val variantChatDataModule =
    module {
        singleOf(::MockChatRetrofitService) { bind<ChatRetrofitService>() }
    }
