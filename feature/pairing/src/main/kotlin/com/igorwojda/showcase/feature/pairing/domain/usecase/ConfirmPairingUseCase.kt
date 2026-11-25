package com.igorwojda.showcase.feature.pairing.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.pairing.domain.model.PairingRoom
import com.igorwojda.showcase.feature.pairing.domain.repository.PairingRepository

/** Confirms the selected pairing package at the current revision. */
internal class ConfirmPairingUseCase(
    private val pairingRepository: PairingRepository,
) {
    /** Confirms the pairing package for the current captain. */
    suspend operator fun invoke(
        roomId: String,
        revision: Int,
        optionId: String,
    ): Result<PairingRoom> = pairingRepository.confirmPairing(roomId = roomId, revision = revision, optionId = optionId)
}
