package com.uzuu.match.data.datasource.api

import android.content.Context
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.uzuu.match.data.datasource.api.model.MatchSummaryDto
import com.uzuu.match.data.datasource.api.response.MatchCostAgreementDto
import com.uzuu.match.data.datasource.api.response.MatchDetailDto
import com.uzuu.match.data.datasource.api.response.MatchDetailResponse
import com.uzuu.match.data.datasource.api.response.MatchListResponse
import com.uzuu.match.data.datasource.api.response.MatchTeamDto
import com.uzuu.match.data.datasource.api.response.MatchVenueDto
import com.uzuu.match.data.datasource.api.service.MatchApi
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

internal class MockMatchDataSource(
    private val context: Context,
    private val json: Json,
) : MatchApi {
    private val addedMatches = CopyOnWriteArrayList<MatchSummaryDto>()
    private val addedDetails = CopyOnWriteArrayList<MatchDetailDto>()

    private fun readAsset(fileName: String): String {
        return try {
            context.assets
                .open(fileName)
                .bufferedReader()
                .use { it.readText() }
        } catch (_: Exception) {
            val overrideProp =
                when (fileName) {
                    "mock/matches.json" -> "matchMockFixturePath"
                    "mock/match-details.json" -> "matchDetailMockFixturePath"
                    else -> null
                }
            val overridePath = overrideProp?.let(System::getProperty)
            if (!overridePath.isNullOrBlank()) {
                val file = File(overridePath)
                if (file.exists()) {
                    return file.readText()
                }
            }
            val fallback = File("src/main/assets", fileName)
            if (fallback.exists()) fallback.readText() else "{}"
        }
    }

    override suspend fun getMatch(matchId: String): ApiResult<MatchDetailResponse> =
        withContext(Dispatchers.IO) {
            val added = addedDetails.firstOrNull { it.matchId == matchId }
            if (added != null) {
                return@withContext ApiResult.Success(MatchDetailResponse(added))
            }
            val text = readAsset("mock/match-details.json")
            val list =
                try {
                    json.decodeFromString<List<MatchDetailDto>>(text)
                } catch (e: Exception) {
                    return@withContext ApiResult.Exception(e)
                }
            val found = list.firstOrNull { it.matchId == matchId }
            if (found != null) {
                ApiResult.Success(MatchDetailResponse(found))
            } else {
                ApiResult.Error(404, "Match not found: $matchId")
            }
        }

    override suspend fun getMatches(): ApiResult<MatchListResponse> =
        withContext(Dispatchers.IO) {
            val text = readAsset("mock/matches.json")
            val response =
                try {
                    json.decodeFromString<MatchListResponse>(text)
                } catch (e: Exception) {
                    return@withContext ApiResult.Exception(e)
                }
            val combined = addedMatches + response.data
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
                        if (draftDto.venueId != null) {
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
