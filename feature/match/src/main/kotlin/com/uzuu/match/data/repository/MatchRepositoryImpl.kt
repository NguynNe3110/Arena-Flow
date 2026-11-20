package com.uzuu.match.data.repository

import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.data.error.httpError
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.data.datasource.api.MatchRemoteDataSource
import com.uzuu.match.data.mapper.dtoToDomain
import com.uzuu.match.domain.model.MatchDetail
import com.uzuu.match.domain.model.MatchDraft
import com.uzuu.match.domain.model.MatchPage
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.domain.model.MercenaryPost
import com.uzuu.match.domain.model.PairingRoom
import com.uzuu.match.domain.model.TeamDetail
import com.uzuu.match.domain.model.TeamDraft
import com.uzuu.match.domain.model.TeamMember
import com.uzuu.match.domain.repository.MatchRepository
import java.time.Instant
import java.util.UUID

internal class MatchRepositoryImpl(
    private val matchRemoteDataSource: MatchRemoteDataSource,
) : MatchRepository {
    override suspend fun getMatch(matchId: String): Result<MatchDetail> =
        dataResult {
            when (val result = matchRemoteDataSource.getMatch(matchId)) {
                is ApiResult.Success -> Result.Success(result.data.data.dtoToDomain())
                is ApiResult.Error -> Result.Failure(httpError(result.code))
                is ApiResult.Exception -> throw result.throwable
            }
        }

    override suspend fun getMatches(): Result<MatchPage> =
        dataResult {
            when (val result = matchRemoteDataSource.getMatches()) {
                is ApiResult.Success -> {
                    val response = result.data
                    val pagination = response.pagination

                    Result.Success(
                        MatchPage(
                            matches = response.data.map { it.dtoToDomain() },
                            page = pagination.page,
                            pageSize = pagination.pageSize,
                            totalPages = pagination.totalPages,
                            totalItems = pagination.totalItems,
                        ),
                    )
                }

                is ApiResult.Error -> {
                    Result.Failure(httpError(result.code))
                }

                is ApiResult.Exception -> {
                    throw result.throwable
                }
            }
        }

    override suspend fun createMatch(draft: MatchDraft): Result<MatchSummary> =
        dataResult {
            val matchId = UUID.randomUUID().toString()
            val dto = com.uzuu.match.data.datasource.api.model.MatchSummaryDto(
                matchId = matchId,
                type = draft.kind.name,
                status = "OPEN",
                sportId = "01930000-0000-7000-8000-000000000001",
                sportName = "Bóng đá",
                format = draft.format,
                skillLevels = listOf(draft.skillLevel),
                startsAt = if (draft.kind == MatchType.HAS_VENUE) "${draft.matchDate}T${draft.startTime}:00Z" else "${draft.matchDate}T18:00:00Z",
                endsAt = if (draft.kind == MatchType.HAS_VENUE) "${draft.matchDate}T${draft.endTime}:00Z" else "${draft.matchDate}T19:30:00Z",
                expiresAt = "${draft.matchDate}T17:00:00Z",
                authorTeamId = draft.hostTeamId,
                authorTeamName = draft.hostTeamName,
                authorTeamReliabilityScore = 100,
                opponentTeamId = null,
                opponentTeamName = null,
                venueId = if (draft.kind == MatchType.HAS_VENUE) draft.venueId else null,
                venueName = if (draft.kind == MatchType.HAS_VENUE) draft.venueName else null,
                venueAddress = if (draft.kind == MatchType.HAS_VENUE) draft.venueAddress else null,
                venueOptionCount = if (draft.kind == MatchType.NEED_VENUE) draft.proposedVenues.size else null,
                timeOptionCount = if (draft.kind == MatchType.NEED_VENUE) draft.proposedTimeSlots.size else null,
                distanceKm = 1.2,
                createdAt = Instant.now().toString(),
            )
            when (val result = matchRemoteDataSource.createMatch(dto)) {
                is ApiResult.Success -> Result.Success(result.data.dtoToDomain())
                is ApiResult.Error -> Result.Failure(httpError(result.code))
                is ApiResult.Exception -> throw result.throwable
            }
        }

    override suspend fun getPairingRoom(pairingId: String): Result<PairingRoom> =
        dataResult {
            when (val result = matchRemoteDataSource.getPairingRoom(pairingId)) {
                is ApiResult.Success -> Result.Success(result.data)
                is ApiResult.Error -> Result.Failure(httpError(result.code))
                is ApiResult.Exception -> throw result.throwable
            }
        }

    override suspend fun confirmPackage(pairingId: String): Result<PairingRoom> =
        dataResult {
            when (val result = matchRemoteDataSource.confirmPackage(pairingId)) {
                is ApiResult.Success -> Result.Success(result.data)
                is ApiResult.Error -> Result.Failure(httpError(result.code))
                is ApiResult.Exception -> throw result.throwable
            }
        }

    override suspend fun updatePackageOption(
        pairingId: String,
        venueId: String?,
        timeSlot: String?,
    ): Result<PairingRoom> =
        dataResult {
            when (val result = matchRemoteDataSource.updatePackageOption(pairingId, venueId, timeSlot)) {
                is ApiResult.Success -> Result.Success(result.data)
                is ApiResult.Error -> Result.Failure(httpError(result.code))
                is ApiResult.Exception -> throw result.throwable
            }
        }

    override suspend fun getMercenaryPosts(): Result<List<MercenaryPost>> =
        dataResult {
            when (val result = matchRemoteDataSource.getMercenaryPosts()) {
                is ApiResult.Success -> Result.Success(result.data)
                is ApiResult.Error -> Result.Failure(httpError(result.code))
                is ApiResult.Exception -> throw result.throwable
            }
        }

    override suspend fun applyMercenary(postId: String, message: String): Result<MercenaryPost> =
        dataResult {
            when (val result = matchRemoteDataSource.applyMercenary(postId, message)) {
                is ApiResult.Success -> Result.Success(result.data)
                is ApiResult.Error -> Result.Failure(httpError(result.code))
                is ApiResult.Exception -> throw result.throwable
            }
        }

    override suspend fun getMyTeam(): Result<TeamDetail> =
        dataResult {
            when (val result = matchRemoteDataSource.getMyTeam()) {
                is ApiResult.Success -> Result.Success(result.data)
                is ApiResult.Error -> Result.Failure(httpError(result.code))
                is ApiResult.Exception -> throw result.throwable
            }
        }

    override suspend fun createTeam(draft: TeamDraft): Result<TeamDetail> =
        dataResult {
            when (val result = matchRemoteDataSource.createTeam(draft)) {
                is ApiResult.Success -> Result.Success(result.data)
                is ApiResult.Error -> Result.Failure(httpError(result.code))
                is ApiResult.Exception -> throw result.throwable
            }
        }

    override suspend fun inviteTeamMember(teamId: String, name: String, position: String): Result<TeamMember> =
        dataResult {
            when (val result = matchRemoteDataSource.inviteTeamMember(teamId, name, position)) {
                is ApiResult.Success -> Result.Success(result.data)
                is ApiResult.Error -> Result.Failure(httpError(result.code))
                is ApiResult.Exception -> throw result.throwable
            }
        }
}
