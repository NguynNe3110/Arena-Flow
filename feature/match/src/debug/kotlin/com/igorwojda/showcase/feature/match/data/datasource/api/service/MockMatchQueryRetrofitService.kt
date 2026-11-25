@file:Suppress("LabeledExpression", "LongParameterList", "MagicNumber")

package com.igorwojda.showcase.feature.match.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginationDto
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchDetailDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchSummaryDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.SportDto
import kotlin.math.max

internal class MockMatchQueryRetrofitService(
    private val store: ArenaInMemoryStore,
) : MatchQueryRetrofitService {
    override suspend fun getMatches(
        type: String?,
        wardId: String?,
        format: String?,
        skillLevel: String?,
        dateFrom: String?,
        dateTo: String?,
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<MatchSummaryDto>> =
        store.executeApi("/api/v1/matches") { snapshot ->
            val filtered =
                snapshot.matches
                    .filter { it.effectiveStatus(snapshot) == "OPEN" }
                    .filter { type.isNullOrBlank() || it.kind.equals(type, ignoreCase = true) }
                    .filter { format.isNullOrBlank() || it.format.name.equals(format, ignoreCase = true) }
                    .filter { skillLevel.isNullOrBlank() || it.skillLevel.label.equals(skillLevel, ignoreCase = true) }
                    .filter { match ->
                        if (wardId.isNullOrBlank()) {
                            true
                        } else {
                            val venueWard = match.venueId?.let { vid -> snapshot.venues.firstOrNull { it.id == vid }?.wardId }
                            val optWards = match.proposedVenues.mapNotNull { pid -> snapshot.venues.firstOrNull { it.id == pid }?.wardId }
                            venueWard == wardId || optWards.contains(wardId)
                        }
                    }.filter { dateFrom.isNullOrBlank() || it.matchDate >= dateFrom.take(10) }
                    .filter { dateTo.isNullOrBlank() || it.matchDate <= dateTo.take(10) }
                    .map { it.toSummaryDto(snapshot) }
            ApiResult.Success(paginate(filtered, page, pageSize))
        }

    override suspend fun getMyMatches(
        teamId: String?,
        status: String?,
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<MatchSummaryDto>> =
        store.executeApi("/api/v1/matches/my") { snapshot ->
            val targetTeamId = teamId ?: snapshot.actingTeamId
            val filtered =
                snapshot.matches
                    .filter { targetTeamId.isNullOrBlank() || it.hostTeamId == targetTeamId || it.guestTeamId == targetTeamId }
                    .filter { status.isNullOrBlank() || status == "ALL" || it.effectiveStatus(snapshot).equals(status, ignoreCase = true) }
                    .map { it.toSummaryDto(snapshot) }
            ApiResult.Success(paginate(filtered, page, pageSize))
        }

    override suspend fun getTeamMatches(
        teamId: String,
        status: String?,
        role: String?,
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<MatchSummaryDto>> =
        store.executeApi("/api/v1/teams/$teamId/matches") { snapshot ->
            val teamExists = snapshot.teams.any { it.id == teamId }
            if (!teamExists) {
                return@executeApi store.apiError(404, "TEAM_001", "Không tìm thấy đội")
            }
            val filtered =
                snapshot.matches
                    .filter { match ->
                        when (role?.uppercase()) {
                            "AUTHOR" -> match.hostTeamId == teamId
                            "OPPONENT" -> match.guestTeamId == teamId
                            else -> match.hostTeamId == teamId || match.guestTeamId == teamId
                        }
                    }.filter { status.isNullOrBlank() || status == "ALL" || it.effectiveStatus(snapshot).equals(status, ignoreCase = true) }
                    .map { it.toSummaryDto(snapshot) }
            ApiResult.Success(paginate(filtered, page, pageSize))
        }

    override suspend fun getMatchDetail(matchId: String): ApiResult<ApiEnvelope<MatchDetailDto>> =
        store.executeApi("/api/v1/matches/$matchId") { snapshot ->
            val match =
                snapshot.matches.firstOrNull { it.id == matchId }
                    ?: return@executeApi store.apiError(404, "MATCH_001", "Không tìm thấy kèo")
            ApiResult.Success(ApiEnvelope(match.toDetailDto(snapshot)))
        }

    override suspend fun getSports(
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<SportDto>> =
        store.executeApi("/api/v1/sports") { snapshot ->
            val activeSports =
                snapshot.sports
                    .filter { it.active }
                    .map { SportDto(sportId = it.id, sportName = it.name, isActive = it.active, createdAt = "2026-11-01T07:00:00") }
            ApiResult.Success(paginate(activeSports, page, pageSize))
        }

    private fun <T> paginate(
        items: List<T>,
        page: Int,
        pageSize: Int,
    ): PaginatedEnvelope<T> {
        val safePage = max(1, page)
        val safeSize = max(1, pageSize)
        val totalItems = items.size
        val totalPages = max(1, (totalItems + safeSize - 1) / safeSize)
        val fromIndex = ((safePage - 1) * safeSize).coerceAtMost(totalItems)
        val toIndex = (fromIndex + safeSize).coerceAtMost(totalItems)
        return PaginatedEnvelope(
            data = items.subList(fromIndex, toIndex),
            pagination = PaginationDto(page = safePage, pageSize = safeSize, totalPages = totalPages, totalItems = totalItems),
        )
    }
}
