package com.uzuu.match.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.uzuu.match.data.datasource.api.model.MatchSummaryDto
import com.uzuu.match.data.datasource.api.response.MatchDetailResponse
import com.uzuu.match.data.datasource.api.response.MatchListResponse
import com.uzuu.match.domain.model.MercenaryPost
import com.uzuu.match.domain.model.PairingRoom
import com.uzuu.match.domain.model.TeamDetail
import com.uzuu.match.domain.model.TeamDraft
import com.uzuu.match.domain.model.TeamMember
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

internal interface MatchApi {

    @GET("matches")
    suspend fun getMatches(): ApiResult<MatchListResponse>

    @GET("matches/{matchId}")
    suspend fun getMatch(@Path("matchId") matchId: String): ApiResult<MatchDetailResponse>

    @POST("matches")
    suspend fun createMatch(@Body draftDto: MatchSummaryDto): ApiResult<MatchSummaryDto>

    @GET("pairings/{pairingId}")
    suspend fun getPairingRoom(@Path("pairingId") pairingId: String): ApiResult<PairingRoom>

    @POST("pairings/{pairingId}/confirm")
    suspend fun confirmPackage(@Path("pairingId") pairingId: String): ApiResult<PairingRoom>

    @POST("pairings/{pairingId}/options")
    suspend fun updatePackageOption(
        @Path("pairingId") pairingId: String,
        @Query("venueId") venueId: String?,
        @Query("timeSlot") timeSlot: String?,
    ): ApiResult<PairingRoom>

    @GET("mercenaries")
    suspend fun getMercenaryPosts(): ApiResult<List<MercenaryPost>>

    @POST("mercenaries/{postId}/apply")
    suspend fun applyMercenary(
        @Path("postId") postId: String,
        @Query("message") message: String,
    ): ApiResult<MercenaryPost>

    @GET("teams/my-team")
    suspend fun getMyTeam(): ApiResult<TeamDetail>

    @POST("teams")
    suspend fun createTeam(@Body draft: TeamDraft): ApiResult<TeamDetail>

    @POST("teams/{teamId}/invite")
    suspend fun inviteTeamMember(
        @Path("teamId") teamId: String,
        @Query("name") name: String,
        @Query("position") position: String,
    ): ApiResult<TeamMember>
}
