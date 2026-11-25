package com.igorwojda.showcase.feature.pairing.data.repository

import com.igorwojda.showcase.feature.base.data.error.apiErrorToAppError
import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.data.error.toAppError
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.CancelPairingRequestDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.ConfirmPairingRequestDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.DisputePairingRequestDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.PairingRoomDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.service.PairingRetrofitService
import com.igorwojda.showcase.feature.pairing.data.mapper.toDomain
import com.igorwojda.showcase.feature.pairing.data.mapper.toDto
import com.igorwojda.showcase.feature.pairing.domain.model.AddPairingOptionInput
import com.igorwojda.showcase.feature.pairing.domain.model.PairingRoom
import com.igorwojda.showcase.feature.pairing.domain.repository.PairingRepository

internal class PairingRepositoryImpl(
    private val pairingRetrofitService: PairingRetrofitService,
) : PairingRepository {
    override suspend fun getPairingRoom(roomId: String): Result<PairingRoom> =
        executeRoomCall { pairingRetrofitService.getPairingRoom(roomId) }

    override suspend fun addPairingOption(
        roomId: String,
        input: AddPairingOptionInput,
    ): Result<PairingRoom> = executeRoomCall { pairingRetrofitService.addPairingOption(roomId, input.toDto()) }

    override suspend fun selectPairingOption(
        roomId: String,
        optionId: String,
    ): Result<PairingRoom> = executeRoomCall { pairingRetrofitService.selectPairingOption(roomId, optionId) }

    override suspend fun confirmPairing(
        roomId: String,
        revision: Int,
        optionId: String,
    ): Result<PairingRoom> =
        executeRoomCall {
            pairingRetrofitService.confirmPairing(
                roomId = roomId,
                request = ConfirmPairingRequestDto(revision = revision, optionId = optionId),
            )
        }

    override suspend fun disputePairing(
        roomId: String,
        reason: String,
    ): Result<PairingRoom> =
        executeRoomCall {
            pairingRetrofitService.disputePairing(
                roomId = roomId,
                request = DisputePairingRequestDto(reason = reason),
            )
        }

    override suspend fun cancelPairing(
        roomId: String,
        reason: String,
    ): Result<PairingRoom> =
        executeRoomCall {
            pairingRetrofitService.cancelPairing(
                roomId = roomId,
                request = CancelPairingRequestDto(reason = reason),
            )
        }

    private suspend fun executeRoomCall(call: suspend () -> ApiResult<ApiEnvelope<PairingRoomDto>>): Result<PairingRoom> =
        dataResult {
            when (val response = call()) {
                is ApiResult.Success -> {
                    Result.Success(response.data.data.toDomain())
                }

                is ApiResult.Error -> {
                    Result.Failure(apiErrorToAppError(response.code, response.errorBody))
                }

                is ApiResult.Exception -> {
                    val appError = (response.throwable as? Exception)?.toAppError() ?: AppError.Unknown
                    Result.Failure(appError)
                }
            }
        }
}
