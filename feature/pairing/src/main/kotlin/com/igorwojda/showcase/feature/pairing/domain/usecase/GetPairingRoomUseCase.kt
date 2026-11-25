package com.igorwojda.showcase.feature.pairing.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.pairing.domain.model.PairingRoom
import com.igorwojda.showcase.feature.pairing.domain.repository.PairingRepository

/** Fetches a pairing negotiation room by its identifier. */
internal class GetPairingRoomUseCase(
    private val pairingRepository: PairingRepository,
) {
    /** Loads the pairing room details. */
    suspend operator fun invoke(roomId: String): Result<PairingRoom> = pairingRepository.getPairingRoom(roomId = roomId)
}
