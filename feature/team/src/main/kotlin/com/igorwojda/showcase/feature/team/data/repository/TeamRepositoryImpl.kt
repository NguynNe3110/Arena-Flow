package com.igorwojda.showcase.feature.team.data.repository

import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.data.datasource.api.model.CreateTeamRequestDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.DisbandTeamRequestDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.TransferCaptainRequestDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.UpdateTeamRequestDto
import com.igorwojda.showcase.feature.team.data.datasource.api.service.TeamRetrofitService
import com.igorwojda.showcase.feature.team.data.mapper.toDomain
import com.igorwojda.showcase.feature.team.data.mapper.toDomainResult
import com.igorwojda.showcase.feature.team.domain.model.CaptainHistoryEntry
import com.igorwojda.showcase.feature.team.domain.model.CaptainTransfer
import com.igorwojda.showcase.feature.team.domain.model.MyTeam
import com.igorwojda.showcase.feature.team.domain.model.TeamDetail
import com.igorwojda.showcase.feature.team.domain.model.TeamDraft
import com.igorwojda.showcase.feature.team.domain.model.TeamSummary
import com.igorwojda.showcase.feature.team.domain.repository.TeamRepository

internal class TeamRepositoryImpl(
    private val teamRetrofitService: TeamRetrofitService,
) : TeamRepository {
    override suspend fun createTeam(draft: TeamDraft): Result<TeamDetail> =
        dataResult {
            teamRetrofitService
                .createTeam(
                    CreateTeamRequestDto(
                        teamName = draft.teamName,
                        avatarUrl = draft.avatarUrl,
                        description = draft.description,
                        skillLevels = draft.skillLevels,
                    ),
                ).toDomainResult { it.data.toDomain() }
        }

    override suspend fun getTeams(
        status: String,
        page: Int,
        pageSize: Int,
    ): Result<List<TeamSummary>> =
        dataResult {
            teamRetrofitService
                .getTeams(status = status, page = page, pageSize = pageSize)
                .toDomainResult { envelope -> envelope.data.map { it.toDomain() } }
        }

    override suspend fun getTeamDetail(teamId: String): Result<TeamDetail> =
        dataResult {
            teamRetrofitService
                .getTeamDetail(teamId = teamId)
                .toDomainResult { it.data.toDomain() }
        }

    override suspend fun updateTeam(
        teamId: String,
        draft: TeamDraft,
    ): Result<TeamDetail> =
        dataResult {
            teamRetrofitService
                .updateTeam(
                    teamId = teamId,
                    body =
                        UpdateTeamRequestDto(
                            teamName = draft.teamName,
                            avatarUrl = draft.avatarUrl,
                            description = draft.description,
                            skillLevels = draft.skillLevels,
                        ),
                ).toDomainResult { it.data.toDomain() }
        }

    override suspend fun disbandTeam(
        teamId: String,
        reason: String?,
    ): Result<TeamDetail> =
        dataResult {
            teamRetrofitService
                .disbandTeam(
                    teamId = teamId,
                    body = DisbandTeamRequestDto(reason = reason),
                ).toDomainResult { it.data.toDomain() }
        }

    override suspend fun getMyTeams(
        role: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<MyTeam>> =
        dataResult {
            teamRetrofitService
                .getMyTeams(role = role, page = page, pageSize = pageSize)
                .toDomainResult { envelope -> envelope.data.map { it.toDomain() } }
        }

    override suspend fun transferCaptain(
        teamId: String,
        newCaptainId: String,
        reason: String?,
    ): Result<CaptainTransfer> =
        dataResult {
            teamRetrofitService
                .transferCaptain(
                    teamId = teamId,
                    body = TransferCaptainRequestDto(newCaptainId = newCaptainId, reason = reason),
                ).toDomainResult { it.data.toDomain() }
        }

    override suspend fun getCaptainHistory(
        teamId: String,
        page: Int,
        pageSize: Int,
    ): Result<List<CaptainHistoryEntry>> =
        dataResult {
            teamRetrofitService
                .getCaptainHistory(teamId = teamId, page = page, pageSize = pageSize)
                .toDomainResult { envelope -> envelope.data.map { it.toDomain() } }
        }
}
