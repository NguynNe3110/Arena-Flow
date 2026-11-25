package com.igorwojda.showcase.feature.chat.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.domain.model.ChatReportReason
import com.igorwojda.showcase.feature.chat.domain.repository.ChatRepository

/** Submits a violation report for a chat room. */
class ReportChatUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        roomId: String,
        reason: ChatReportReason,
        details: String,
    ): Result<Unit> = chatRepository.reportChat(roomId = roomId, reason = reason, details = details)
}
