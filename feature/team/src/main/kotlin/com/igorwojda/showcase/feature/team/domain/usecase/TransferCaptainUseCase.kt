package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.CaptainTransfer
import com.igorwojda.showcase.feature.team.domain.repository.TeamRepository

internal class TransferCaptainUseCase(
    private val teamRepository: TeamRepository,
) {
    suspend operator fun invoke(
        teamId: String,
        newCaptainId: String,
        reason: String? = null,
    ): Result<CaptainTransfer> =
        teamRepository.transferCaptain(
            teamId = teamId,
            newCaptainId = newCaptainId,
            reason = reason,
        )
}
