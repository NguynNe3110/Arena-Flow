package com.igorwojda.showcase.feature.pairing.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.pairing.domain.model.AddPairingOptionInput
import com.igorwojda.showcase.feature.pairing.domain.model.PairingRoom
import com.igorwojda.showcase.feature.pairing.domain.repository.PairingRepository

/** Proposes a new venue, time, and fee split option in a pairing room. */
internal class AddPairingOptionUseCase(
    private val pairingRepository: PairingRepository,
) {
    /** Submits a new pairing option for the given room. */
    suspend operator fun invoke(
        roomId: String,
        input: AddPairingOptionInput,
    ): Result<PairingRoom> = pairingRepository.addPairingOption(roomId = roomId, input = input)
}
