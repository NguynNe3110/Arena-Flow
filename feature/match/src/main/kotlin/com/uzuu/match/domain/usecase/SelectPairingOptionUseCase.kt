package com.uzuu.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.PairingRoom
import com.uzuu.match.domain.repository.MatchRepository

internal class SelectPairingOptionUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(
        pairingId: String,
        venueId: String?,
        timeSlot: String?,
    ): Result<PairingRoom> = matchRepository.updatePackageOption(pairingId, venueId, timeSlot)
}
