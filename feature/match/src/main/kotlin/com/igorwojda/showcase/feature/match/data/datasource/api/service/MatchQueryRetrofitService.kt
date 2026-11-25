@file:Suppress("LongParameterList")

package com.igorwojda.showcase.feature.match.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchDetailDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchSummaryDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.SportDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Read-only Retrofit endpoints for match discovery, details, and sports catalog. */
interface MatchQueryRetrofitService {
    @GET("matches")
    suspend fun getMatches(
        @Query("type") type: String? = null,
        @Query("wardId") wardId: String? = null,
        @Query("format") format: String? = null,
        @Query("skillLevel") skillLevel: String? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<MatchSummaryDto>>

    @GET("matches/my")
    suspend fun getMyMatches(
        @Query("teamId") teamId: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<MatchSummaryDto>>

    @GET("teams/{teamId}/matches")
    suspend fun getTeamMatches(
        @Path("teamId") teamId: String,
        @Query("status") status: String? = null,
        @Query("role") role: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<MatchSummaryDto>>

    @GET("matches/{matchId}")
    suspend fun getMatchDetail(
        @Path("matchId") matchId: String,
    ): ApiResult<ApiEnvelope<MatchDetailDto>>

    @GET("sports")
    suspend fun getSports(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
    ): ApiResult<PaginatedEnvelope<SportDto>>
}
