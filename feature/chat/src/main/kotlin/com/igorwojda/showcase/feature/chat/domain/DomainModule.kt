package com.igorwojda.showcase.feature.chat.domain

import com.igorwojda.showcase.feature.chat.domain.usecase.GetChatMessagesUseCase
import com.igorwojda.showcase.feature.chat.domain.usecase.GetChatRoomDetailUseCase
import com.igorwojda.showcase.feature.chat.domain.usecase.GetChatRoomsUseCase
import com.igorwojda.showcase.feature.chat.domain.usecase.MarkChatReadUseCase
import com.igorwojda.showcase.feature.chat.domain.usecase.ReportChatUseCase
import com.igorwojda.showcase.feature.chat.domain.usecase.SendChatMessageUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val chatDomainModule =
    module {
        singleOf(::GetChatRoomsUseCase)
        singleOf(::GetChatRoomDetailUseCase)
        singleOf(::GetChatMessagesUseCase)
        singleOf(::SendChatMessageUseCase)
        singleOf(::MarkChatReadUseCase)
        singleOf(::ReportChatUseCase)
    }
