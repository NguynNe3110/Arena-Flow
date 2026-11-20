package com.uzuu.pairing.data.repository

import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.pairing.data.datasource.api.PairingRemoteDataSource
import com.uzuu.pairing.domain.model.PairingRoom
import com.uzuu.pairing.domain.repository.PairingRepository

internal class PairingRepositoryImpl(
    private val pairingRemoteDataSource: PairingRemoteDataSource,
) : PairingRepository {
    override suspend fun getPairingRoom(pairingId: String): Result<PairingRoom> =
        dataResult {
            Result.Success(pairingRemoteDataSource.getPairingRoom(pairingId))
        }

    override suspend fun updatePackageOption(
        pairingId: String,
        venueId: String?,
        timeSlot: String?,
    ): Result<PairingRoom> =
        dataResult {
            Result.Success(pairingRemoteDataSource.updatePackageOption(pairingId, venueId, timeSlot))
        }

    override suspend fun confirmPackage(pairingId: String): Result<PairingRoom> =
        dataResult {
            Result.Success(pairingRemoteDataSource.confirmPackage(pairingId))
        }
}
