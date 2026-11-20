package com.uzuu.match.data.datasource.api

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.uzuu.match.data.datasource.api.response.MatchListResponse
import com.uzuu.match.data.datasource.api.response.MatchDetailResponse
import com.uzuu.match.data.datasource.api.service.MatchApi

internal class MatchRemoteDataSource(
    private val api: MatchApi,
) {
    suspend fun getMatch(matchId: String): ApiResult<MatchDetailResponse> = api.getMatch(matchId)

    suspend fun createMatch(draftDto: com.uzuu.match.data.datasource.api.model.MatchSummaryDto): ApiResult<com.uzuu.match.data.datasource.api.model.MatchSummaryDto> =
        api.createMatch(draftDto)

    suspend fun getPairingRoom(pairingId: String): ApiResult<com.uzuu.match.domain.model.PairingRoom> =
        api.getPairingRoom(pairingId)

    suspend fun confirmPackage(pairingId: String): ApiResult<com.uzuu.match.domain.model.PairingRoom> =
        api.confirmPackage(pairingId)

    suspend fun updatePackageOption(
        pairingId: String,
        venueId: String?,
        timeSlot: String?,
    ): ApiResult<com.uzuu.match.domain.model.PairingRoom> =
        api.updatePackageOption(pairingId, venueId, timeSlot)

    suspend fun getMercenaryPosts(): ApiResult<List<com.uzuu.match.domain.model.MercenaryPost>> =
        api.getMercenaryPosts()

    suspend fun applyMercenary(postId: String, message: String): ApiResult<com.uzuu.match.domain.model.MercenaryPost> =
        api.applyMercenary(postId, message)

    suspend fun getMatches(): ApiResult<MatchListResponse> {
        return api.getMatches()
    }
}
