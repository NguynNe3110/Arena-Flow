package com.uzuu.match.data.datasource.api

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.uzuu.match.data.datasource.api.model.MatchSummaryDto
import com.uzuu.match.data.datasource.api.response.MatchDetailResponse
import com.uzuu.match.data.datasource.api.response.MatchListResponse
import com.uzuu.match.data.datasource.api.service.MatchApi
import com.uzuu.match.domain.model.MercenaryPost
import com.uzuu.match.domain.model.PairingRoom
import com.uzuu.match.domain.model.TeamDetail
import com.uzuu.match.domain.model.TeamDraft
import com.uzuu.match.domain.model.TeamMember

internal class MatchRemoteDataSource(
    private val api: MatchApi,
) {
    suspend fun getMatch(matchId: String): ApiResult<MatchDetailResponse> = api.getMatch(matchId)

    suspend fun createMatch(draftDto: MatchSummaryDto): ApiResult<MatchSummaryDto> =
        api.createMatch(draftDto)

    suspend fun getPairingRoom(pairingId: String): ApiResult<PairingRoom> =
        api.getPairingRoom(pairingId)

    suspend fun confirmPackage(pairingId: String): ApiResult<PairingRoom> =
        api.confirmPackage(pairingId)

    suspend fun updatePackageOption(
        pairingId: String,
        venueId: String?,
        timeSlot: String?,
    ): ApiResult<PairingRoom> =
        api.updatePackageOption(pairingId, venueId, timeSlot)

    suspend fun getMercenaryPosts(): ApiResult<List<MercenaryPost>> =
        api.getMercenaryPosts()

    suspend fun applyMercenary(postId: String, message: String): ApiResult<MercenaryPost> =
        api.applyMercenary(postId, message)

    suspend fun getMatches(): ApiResult<MatchListResponse> = api.getMatches()

    suspend fun getMyTeam(): ApiResult<TeamDetail> = api.getMyTeam()

    suspend fun createTeam(draft: TeamDraft): ApiResult<TeamDetail> = api.createTeam(draft)

    suspend fun inviteTeamMember(teamId: String, name: String, position: String): ApiResult<TeamMember> =
        api.inviteTeamMember(teamId, name, position)
}
