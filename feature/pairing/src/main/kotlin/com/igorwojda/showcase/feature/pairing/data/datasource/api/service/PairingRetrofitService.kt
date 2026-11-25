package com.igorwojda.showcase.feature.pairing.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.AddPairingOptionRequestDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.CancelPairingRequestDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.ConfirmPairingRequestDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.DisputePairingRequestDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.PairingRoomDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Retrofit service for MATCH-002 pairing room endpoints. */
internal interface PairingRetrofitService {
    @GET("pairing-rooms/{roomId}")
    suspend fun getPairingRoom(
        @Path("roomId") roomId: String,
    ): ApiResult<ApiEnvelope<PairingRoomDto>>

    @POST("pairing-rooms/{roomId}/options")
    suspend fun addPairingOption(
        @Path("roomId") roomId: String,
        @Body request: AddPairingOptionRequestDto,
    ): ApiResult<ApiEnvelope<PairingRoomDto>>

    @POST("pairing-rooms/{roomId}/options/{optionId}/select")
    suspend fun selectPairingOption(
        @Path("roomId") roomId: String,
        @Path("optionId") optionId: String,
    ): ApiResult<ApiEnvelope<PairingRoomDto>>

    @POST("pairing-rooms/{roomId}/confirm")
    suspend fun confirmPairing(
        @Path("roomId") roomId: String,
        @Body request: ConfirmPairingRequestDto,
    ): ApiResult<ApiEnvelope<PairingRoomDto>>

    @POST("pairing-rooms/{roomId}/dispute")
    suspend fun disputePairing(
        @Path("roomId") roomId: String,
        @Body request: DisputePairingRequestDto,
    ): ApiResult<ApiEnvelope<PairingRoomDto>>

    @POST("pairing-rooms/{roomId}/cancel")
    suspend fun cancelPairing(
        @Path("roomId") roomId: String,
        @Body request: CancelPairingRequestDto,
    ): ApiResult<ApiEnvelope<PairingRoomDto>>
}
