package com.igorwojda.showcase.feature.pairing.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.pairing.domain.model.PairingRoom
import com.igorwojda.showcase.feature.pairing.domain.repository.PairingRepository

/** Selects an option inside a pairing negotiation room. */
internal class SelectPairingOptionUseCase(
    private val pairingRepository: PairingRepository,
) {
    /** Marks the specified option as selected in the pairing room. */
    suspend operator fun invoke(
        roomId: String,
        optionId: String,
    ): Result<PairingRoom> = pairingRepository.selectPairingOption(roomId = roomId, optionId = optionId)
}
