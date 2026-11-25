@file:Suppress("LongParameterList")

package com.igorwojda.showcase.feature.venue.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.CreateVenueRequestDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.ProvinceItemDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.ReportVenueRequestDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.UpdateVenueRequestDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.VenueDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.VenueReportDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.VenueSummaryDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.WardItemDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VenueRetrofitService {
    @GET("venues")
    suspend fun getVenues(
        @Query("q") q: String? = null,
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("radiusKm") radiusKm: Double? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<VenueSummaryDto>>

    @POST("venues")
    suspend fun createVenue(
        @Body request: CreateVenueRequestDto,
    ): ApiResult<ApiEnvelope<VenueDto>>

    @GET("venues/{venueId}")
    suspend fun getVenueDetail(
        @Path("venueId") venueId: String,
    ): ApiResult<ApiEnvelope<VenueDto>>

    @PATCH("venues/{venueId}")
    suspend fun updateVenue(
        @Path("venueId") venueId: String,
        @Body request: UpdateVenueRequestDto,
    ): ApiResult<ApiEnvelope<VenueDto>>

    @POST("venues/{venueId}/reports")
    suspend fun reportVenue(
        @Path("venueId") venueId: String,
        @Body request: ReportVenueRequestDto,
    ): ApiResult<ApiEnvelope<VenueReportDto>>

    @GET("provinces")
    suspend fun getProvinces(): ApiResult<ApiEnvelope<List<ProvinceItemDto>>>

    @GET("provinces/{provinceId}/wards")
    suspend fun getWardsByProvince(
        @Path("provinceId") provinceId: String,
    ): ApiResult<ApiEnvelope<List<WardItemDto>>>
}
