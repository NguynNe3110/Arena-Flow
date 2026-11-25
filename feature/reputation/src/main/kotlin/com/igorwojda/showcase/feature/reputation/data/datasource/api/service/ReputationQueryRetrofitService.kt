package com.igorwojda.showcase.feature.reputation.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.AdminDisputeDetailDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.AdminDisputeSummaryDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.MatchRatingsResponseDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.ReportDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.TeamReputationDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Read-only Retrofit endpoints for REPUTATION-001 (5 methods <= 8). */
interface ReputationQueryRetrofitService {
    @GET("matches/{matchId}/ratings")
    suspend fun getMatchRatings(
        @Path("matchId") matchId: String,
        @Query("teamId") teamId: String,
    ): ApiResult<ApiEnvelope<MatchRatingsResponseDto>>

    @GET("teams/{teamId}/reports")
    suspend fun getTeamReports(
        @Path("teamId") teamId: String,
        @Query("direction") direction: String = "RECEIVED",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<ReportDto>>

    @GET("teams/{teamId}/reputation")
    suspend fun getTeamReputation(
        @Path("teamId") teamId: String,
    ): ApiResult<ApiEnvelope<TeamReputationDto>>

    @GET("admin/disputes")
    suspend fun getAdminDisputes(
        @Query("status") status: String = "PENDING",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<AdminDisputeSummaryDto>>

    @GET("admin/disputes/{disputeId}")
    suspend fun getAdminDisputeDetail(
        @Path("disputeId") disputeId: String,
    ): ApiResult<ApiEnvelope<AdminDisputeDetailDto>>
}
