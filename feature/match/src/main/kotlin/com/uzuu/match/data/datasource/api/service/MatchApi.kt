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
    suspend fun getMatch(@Path("matchId") matchId: String): ApiResult<MatchDetailResponse>

    @retrofit2.http.POST("matches")
    suspend fun createMatch(@retrofit2.http.Body draftDto: com.uzuu.match.data.datasource.api.model.MatchSummaryDto): ApiResult<com.uzuu.match.data.datasource.api.model.MatchSummaryDto>

}
