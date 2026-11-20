package com.uzuu.match.data.datasource.api

import android.content.Context
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.uzuu.match.data.datasource.api.model.MatchSummaryDto
import com.uzuu.match.data.datasource.api.response.MatchDetailDto
import com.uzuu.match.data.datasource.api.response.MatchDetailResponse
import com.uzuu.match.data.datasource.api.response.MatchListResponse
import com.uzuu.match.data.datasource.api.response.MatchTeamDto
import com.uzuu.match.data.datasource.api.response.MatchVenueDto
import com.uzuu.match.data.datasource.api.response.MatchCostAgreementDto
import com.uzuu.match.data.datasource.api.service.MatchApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.concurrent.CopyOnWriteArrayList

internal class MockMatchDataSource(
    private val context: Context,
    private val json: Json,
) : MatchApi {
    private val addedMatches = CopyOnWriteArrayList<MatchSummaryDto>()
    private val addedDetails = CopyOnWriteArrayList<MatchDetailDto>()

    override suspend fun getMatch(matchId: String): ApiResult<MatchDetailResponse> =
        withContext(Dispatchers.IO) {
            val added = addedDetails.find { it.matchId == matchId }
            if (added != null) return@withContext ApiResult.Success(MatchDetailResponse(added))

            val content =
                context.assets
                    .open("mock/match-details.json")
                    .bufferedReader()
                    .use { it.readText() }
            val match = json.decodeFromString<List<MatchDetailDto>>(content).find { it.matchId == matchId }
            if (match == null) {
                ApiResult.Error(404, "Không tìm thấy kèo")
            } else {
                ApiResult.Success(MatchDetailResponse(match))
            }
        }

    override suspend fun getMatches(): ApiResult<MatchListResponse> =
        withContext(Dispatchers.IO) {
            val content =
                context.assets
                    .open("mock/matches.json")
                    .bufferedReader()
                    .use { reader -> reader.readText() }

            val response =
                json.decodeFromString<MatchListResponse>(content)

            val combined = addedMatches.toList() + response.data
            ApiResult.Success(
                response.copy(
                    data = combined,
                    pagination = response.pagination.copy(totalItems = combined.size),
                ),
            )
        }

    override suspend fun createMatch(draftDto: MatchSummaryDto): ApiResult<MatchSummaryDto> =
        withContext(Dispatchers.IO) {
            addedMatches.add(0, draftDto)
            addedDetails.add(
                0,
                MatchDetailDto(
                    matchId = draftDto.matchId,
                    type = draftDto.type,
                    status = draftDto.status,
                    format = draftDto.format,
                    skillLevels = draftDto.skillLevels,
                    startsAt = draftDto.startsAt,
                    endsAt = draftDto.endsAt,
                    expiresAt = draftDto.expiresAt,
                    authorTeam = MatchTeamDto(draftDto.authorTeamId, draftDto.authorTeamName, draftDto.authorTeamReliabilityScore),
                    venue =
                        if (draftDto.venueId !=
                            null
                        ) {
                            MatchVenueDto(draftDto.venueId, draftDto.venueName ?: "", draftDto.venueAddress ?: "")
                        } else {
                            null
                        },
                    costAgreement = MatchCostAgreementDto(),
                ),
            )
            ApiResult.Success(draftDto)
        }
}
