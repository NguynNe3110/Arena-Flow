package com.igorwojda.showcase.feature.team.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.team.data.datasource.api.model.CaptainHistoryDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.CaptainTransferDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.CreateTeamRequestDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.DisbandTeamRequestDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.MyTeamDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.TeamDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.TeamSummaryDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.TransferCaptainRequestDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.UpdateTeamRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

internal interface TeamRetrofitService {
    @POST("api/v1/teams")
    suspend fun createTeam(
        @Body body: CreateTeamRequestDto,
    ): ApiResult<ApiEnvelope<TeamDto>>

    @GET("api/v1/teams")
    suspend fun getTeams(
        @Query("status") status: String = "ACTIVE",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<TeamSummaryDto>>

    @GET("api/v1/teams/{teamId}")
    suspend fun getTeamDetail(
        @Path("teamId") teamId: String,
    ): ApiResult<ApiEnvelope<TeamDto>>

    @PATCH("api/v1/teams/{teamId}")
    suspend fun updateTeam(
        @Path("teamId") teamId: String,
        @Body body: UpdateTeamRequestDto,
    ): ApiResult<ApiEnvelope<TeamDto>>

    @POST("api/v1/teams/{teamId}/disband")
    suspend fun disbandTeam(
        @Path("teamId") teamId: String,
        @Body body: DisbandTeamRequestDto,
    ): ApiResult<ApiEnvelope<TeamDto>>

    @GET("api/v1/profile/teams")
    suspend fun getMyTeams(
        @Query("role") role: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<MyTeamDto>>

    @POST("api/v1/teams/{teamId}/captain/transfer")
    suspend fun transferCaptain(
        @Path("teamId") teamId: String,
        @Body body: TransferCaptainRequestDto,
    ): ApiResult<ApiEnvelope<CaptainTransferDto>>

    @GET("api/v1/teams/{teamId}/captain/history")
    suspend fun getCaptainHistory(
        @Path("teamId") teamId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<CaptainHistoryDto>>
}
