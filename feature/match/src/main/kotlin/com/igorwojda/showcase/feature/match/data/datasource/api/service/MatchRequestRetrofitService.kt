package com.igorwojda.showcase.feature.match.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.match.data.datasource.api.model.AcceptMatchRequestResponseDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.RejectMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.SendMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.WithdrawMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.WithdrawMatchRequestResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit endpoints for match join requests (flow A). */
interface MatchRequestRetrofitService {
    @POST("matches/{matchId}/requests")
    suspend fun sendMatchRequest(
        @Path("matchId") matchId: String,
        @Body body: SendMatchRequestDto,
    ): ApiResult<ApiEnvelope<MatchRequestDto>>

    @GET("matches/{matchId}/requests")
    suspend fun getMatchRequests(
        @Path("matchId") matchId: String,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<MatchRequestDto>>

    @GET("teams/{teamId}/match-requests")
    suspend fun getTeamMatchRequests(
        @Path("teamId") teamId: String,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<MatchRequestDto>>

    @POST("match-requests/{requestId}/accept")
    suspend fun acceptMatchRequest(
        @Path("requestId") requestId: String,
    ): ApiResult<ApiEnvelope<AcceptMatchRequestResponseDto>>

    @POST("match-requests/{requestId}/reject")
    suspend fun rejectMatchRequest(
        @Path("requestId") requestId: String,
        @Body body: RejectMatchRequestDto? = null,
    ): ApiResult<Unit>

    @POST("match-requests/{requestId}/withdraw")
    suspend fun withdrawMatchRequest(
        @Path("requestId") requestId: String,
        @Body body: WithdrawMatchRequestDto,
    ): ApiResult<ApiEnvelope<WithdrawMatchRequestResponseDto>>
}
