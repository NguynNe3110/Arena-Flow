package com.igorwojda.showcase.feature.pairing.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.pairing.domain.model.PairingRoom
import com.igorwojda.showcase.feature.pairing.domain.repository.PairingRepository

/** Cancels a pairing negotiation room with a reason. */
internal class CancelPairingUseCase(
    private val pairingRepository: PairingRepository,
) {
    /** Cancels the pairing room with the given reason. */
    suspend operator fun invoke(
        roomId: String,
        reason: String,
    ): Result<PairingRoom> = pairingRepository.cancelPairing(roomId = roomId, reason = reason)
}
