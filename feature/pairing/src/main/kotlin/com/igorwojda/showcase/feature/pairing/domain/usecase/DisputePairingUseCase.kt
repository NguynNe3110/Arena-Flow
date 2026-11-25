package com.igorwojda.showcase.feature.pairing.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.pairing.domain.model.PairingRoom
import com.igorwojda.showcase.feature.pairing.domain.repository.PairingRepository

/** Submits a dispute for a pairing negotiation room. */
internal class DisputePairingUseCase(
    private val pairingRepository: PairingRepository,
) {
    /** Transitions the pairing room to disputed state with the given reason. */
    suspend operator fun invoke(
        roomId: String,
        reason: String,
    ): Result<PairingRoom> = pairingRepository.disputePairing(roomId = roomId, reason = reason)
}
