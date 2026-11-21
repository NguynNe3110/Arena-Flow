package com.uzuu.pairing.data.datasource.api

import com.uzuu.pairing.domain.model.PairingRoom

interface PairingRemoteDataSource {
    suspend fun getPairingRoom(pairingId: String): PairingRoom

    suspend fun updatePackageOption(
        pairingId: String,
        venueId: String?,
        timeSlot: String?,
    ): PairingRoom

    suspend fun confirmPackage(pairingId: String): PairingRoom
}
