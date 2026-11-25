package com.igorwojda.showcase.feature.admin.data.datasource.api.service

import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdminPendingVenueDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdminReportDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.RejectVenueRequestDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.ResolveReportRequestDto
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit service for admin report and venue moderation endpoints. */
interface AdminModerationRetrofitService {
    @GET("admin/reports")
    suspend fun getReports(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<AdminReportDto>>

    @POST("admin/reports/{reportId}/resolve")
    suspend fun resolveReport(
        @Path("reportId") reportId: String,
        @Body request: ResolveReportRequestDto,
    ): ApiResult<ApiEnvelope<AdminReportDto>>

    @GET("admin/venues/pending")
    suspend fun getPendingVenues(): ApiResult<ApiEnvelope<List<AdminPendingVenueDto>>>

    @POST("admin/venues/{venueId}/approve")
    suspend fun approveVenue(
        @Path("venueId") venueId: String,
    ): ApiResult<Unit>

    @POST("admin/venues/{venueId}/reject")
    suspend fun rejectVenue(
        @Path("venueId") venueId: String,
        @Body request: RejectVenueRequestDto,
    ): ApiResult<Unit>
}
