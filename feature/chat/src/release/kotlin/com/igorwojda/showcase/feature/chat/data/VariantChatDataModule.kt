package com.igorwojda.showcase.feature.chat.data

import com.igorwojda.showcase.feature.chat.data.datasource.api.service.ChatRetrofitService
import org.koin.dsl.module
import retrofit2.Retrofit

internal val variantChatDataModule =
    module {
        single { get<Retrofit>().create(ChatRetrofitService::class.java) }
    }
