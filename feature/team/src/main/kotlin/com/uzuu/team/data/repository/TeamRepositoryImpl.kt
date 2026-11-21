package com.uzuu.team.data.repository

import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.team.data.datasource.api.TeamRemoteDataSource
import com.uzuu.team.domain.model.TeamDetail
import com.uzuu.team.domain.model.TeamDraft
import com.uzuu.team.domain.model.TeamMember
import com.uzuu.team.domain.repository.TeamRepository

internal class TeamRepositoryImpl(
    private val teamRemoteDataSource: TeamRemoteDataSource,
) : TeamRepository {
    override suspend fun getMyTeam(): Result<TeamDetail> =
        dataResult {
            Result.Success(teamRemoteDataSource.getMyTeam())
        }

    override suspend fun createTeam(draft: TeamDraft): Result<TeamDetail> =
        dataResult {
            Result.Success(teamRemoteDataSource.createTeam(draft))
        }

    override suspend fun inviteTeamMember(
        teamId: String,
        name: String,
        position: String,
    ): Result<TeamMember> =
        dataResult {
            Result.Success(teamRemoteDataSource.inviteTeamMember(teamId, name, position))
        }
}
