package com.uzuu.pairing.domain.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.pairing.domain.model.PairingRoom

internal interface PairingRepository {
    suspend fun getPairingRoom(pairingId: String): Result<PairingRoom>

    suspend fun updatePackageOption(
        pairingId: String,
        venueId: String?,
        timeSlot: String?,
    ): Result<PairingRoom>

    suspend fun confirmPackage(pairingId: String): Result<PairingRoom>
}
