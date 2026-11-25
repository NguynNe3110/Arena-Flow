package com.igorwojda.showcase.feature.match.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.match.data.datasource.api.model.CancelMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.CancelMatchResponseDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.CostAgreementRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.CostAgreementResponseDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.CreateMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.ForfeitMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchDetailDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.UpdateMatchRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Write Retrofit endpoints for match creation, editing, cancellation, and forfeit. */
interface MatchCommandRetrofitService {
    @POST("matches")
    suspend fun createMatch(
        @Body body: CreateMatchRequestDto,
    ): ApiResult<ApiEnvelope<MatchDetailDto>>

    @PUT("matches/{matchId}")
    suspend fun updateMatch(
        @Path("matchId") matchId: String,
        @Body body: UpdateMatchRequestDto,
    ): ApiResult<ApiEnvelope<MatchDetailDto>>

    @DELETE("matches/{matchId}")
    suspend fun deleteMatch(
        @Path("matchId") matchId: String,
    ): ApiResult<Unit>

    @PUT("matches/{matchId}/cost-agreement")
    suspend fun updateCostAgreement(
        @Path("matchId") matchId: String,
        @Body body: CostAgreementRequestDto,
    ): ApiResult<ApiEnvelope<CostAgreementResponseDto>>

    @POST("matches/{matchId}/cancel")
    suspend fun cancelMatch(
        @Path("matchId") matchId: String,
        @Body body: CancelMatchRequestDto,
    ): ApiResult<ApiEnvelope<CancelMatchResponseDto>>

    @POST("matches/{matchId}/forfeit")
    suspend fun forfeitMatch(
        @Path("matchId") matchId: String,
        @Body body: ForfeitMatchRequestDto,
    ): ApiResult<Unit>
}
