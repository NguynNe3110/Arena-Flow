package com.uzuu.match.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.uzuu.match.data.datasource.api.response.MatchListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import com.uzuu.match.data.datasource.api.response.MatchDetailResponse

internal interface MatchApi {
    @GET("matches")
    suspend fun getMatches(): ApiResult<MatchListResponse>

    @GET("matches/{matchId}")
    suspend fun getMatch(
        @Path("matchId") matchId: String,
    ): ApiResult<MatchDetailResponse>

    @retrofit2.http.POST("matches")
    suspend fun createMatch(
        @retrofit2.http.Body draftDto: com.uzuu.match.data.datasource.api.model.MatchSummaryDto,
    ): ApiResult<com.uzuu.match.data.datasource.api.model.MatchSummaryDto>

    @GET("pairings/{pairingId}")
    suspend fun getPairingRoom(
        @Path("pairingId") pairingId: String,
    ): ApiResult<com.uzuu.match.domain.model.PairingRoom>

    @retrofit2.http.POST("pairings/{pairingId}/confirm")
    suspend fun confirmPackage(
        @Path("pairingId") pairingId: String,
    ): ApiResult<com.uzuu.match.domain.model.PairingRoom>

    @retrofit2.http.POST("pairings/{pairingId}/options")
    suspend fun updatePackageOption(
        @Path("pairingId") pairingId: String,
        @retrofit2.http.Query("venueId") venueId: String?,
        @retrofit2.http.Query("timeSlot") timeSlot: String?,
    ): ApiResult<com.uzuu.match.domain.model.PairingRoom>

    @GET("mercenaries")
    suspend fun getMercenaryPosts(): ApiResult<List<com.uzuu.match.domain.model.MercenaryPost>>

    @retrofit2.http.POST("mercenaries/{postId}/apply")
    suspend fun applyMercenary(
        @Path("postId") postId: String,
        @retrofit2.http.Query("message") message: String,
    ): ApiResult<com.uzuu.match.domain.model.MercenaryPost>
}
