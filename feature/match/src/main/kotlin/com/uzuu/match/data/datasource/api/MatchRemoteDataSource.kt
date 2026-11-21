package com.uzuu.match.data.datasource.api

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.uzuu.match.data.datasource.api.model.MatchSummaryDto
import com.uzuu.match.data.datasource.api.response.MatchDetailResponse
import com.uzuu.match.data.datasource.api.response.MatchListResponse
import com.uzuu.match.data.datasource.api.service.MatchApi

internal class MatchRemoteDataSource(
    private val api: MatchApi,
) {
    suspend fun getMatch(matchId: String): ApiResult<MatchDetailResponse> = api.getMatch(matchId)

    suspend fun createMatch(draftDto: MatchSummaryDto): ApiResult<MatchSummaryDto> = api.createMatch(draftDto)

    suspend fun getMatches(): ApiResult<MatchListResponse> = api.getMatches()
}
