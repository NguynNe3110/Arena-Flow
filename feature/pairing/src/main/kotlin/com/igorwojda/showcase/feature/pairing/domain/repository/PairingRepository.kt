package com.igorwojda.showcase.feature.pairing.domain.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.pairing.domain.model.AddPairingOptionInput
import com.igorwojda.showcase.feature.pairing.domain.model.PairingRoom

/** Repository contract for Match B pairing negotiation rooms. */
internal interface PairingRepository {
    /** Fetches the current state of a pairing negotiation room. */
    suspend fun getPairingRoom(roomId: String): Result<PairingRoom>

    /** Proposes a new venue, time slot, and fee split option in the pairing room. */
    suspend fun addPairingOption(
        roomId: String,
        input: AddPairingOptionInput,
    ): Result<PairingRoom>

    /** Selects an existing option in the pairing room for the current captain. */
    suspend fun selectPairingOption(
        roomId: String,
        optionId: String,
    ): Result<PairingRoom>

    /** Confirms the agreed package at the given revision and optionId. */
    suspend fun confirmPairing(
        roomId: String,
        revision: Int,
        optionId: String,
    ): Result<PairingRoom>

    /** Disputes the pairing room with a required reason. */
    suspend fun disputePairing(
        roomId: String,
        reason: String,
    ): Result<PairingRoom>

    /** Cancels the pairing room with a required reason. */
    suspend fun cancelPairing(
        roomId: String,
        reason: String,
    ): Result<PairingRoom>
}
