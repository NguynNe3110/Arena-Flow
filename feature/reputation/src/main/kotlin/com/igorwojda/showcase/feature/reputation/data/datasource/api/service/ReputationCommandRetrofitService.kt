package com.igorwojda.showcase.feature.reputation.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.AdminDisputeDetailDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.DisputeCreatedDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.DisputeReportRequestDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.MatchRatingDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.ReportDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.ResolveDisputeRequestDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.SubmitChatReportRequestDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.SubmitMatchRatingRequestDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.SubmitMatchReportRequestDto
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Write Retrofit endpoints for REPUTATION-001 (5 methods <= 8). */
interface ReputationCommandRetrofitService {
    @POST("matches/{matchId}/ratings")
    suspend fun submitMatchRating(
        @Path("matchId") matchId: String,
        @Body request: SubmitMatchRatingRequestDto,
    ): ApiResult<ApiEnvelope<MatchRatingDto>>

    @POST("matches/{matchId}/reports")
    suspend fun submitMatchReport(
        @Path("matchId") matchId: String,
        @Body request: SubmitMatchReportRequestDto,
    ): ApiResult<ApiEnvelope<ReportDto>>

    @POST("reports/{reportId}/dispute")
    suspend fun disputeReport(
        @Path("reportId") reportId: String,
        @Body request: DisputeReportRequestDto,
    ): ApiResult<ApiEnvelope<DisputeCreatedDto>>

    @PATCH("admin/disputes/{disputeId}/resolve")
    suspend fun resolveAdminDispute(
        @Path("disputeId") disputeId: String,
        @Body request: ResolveDisputeRequestDto,
    ): ApiResult<ApiEnvelope<AdminDisputeDetailDto>>

    @POST("conversations/{conversationId}/reports")
    suspend fun submitChatReport(
        @Path("conversationId") conversationId: String,
        @Body request: SubmitChatReportRequestDto,
    ): ApiResult<ApiEnvelope<ReportDto>>
}
