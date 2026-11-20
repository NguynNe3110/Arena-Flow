package com.uzuu.pairing.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.pairing.domain.model.PairingRoom
import com.uzuu.pairing.domain.repository.PairingRepository

internal class SelectPairingOptionUseCase(
    private val pairingRepository: PairingRepository,
) {
    suspend operator fun invoke(
        pairingId: String,
        venueId: String?,
        timeSlot: String?,
    ): Result<PairingRoom> = pairingRepository.updatePackageOption(pairingId, venueId, timeSlot)
}
