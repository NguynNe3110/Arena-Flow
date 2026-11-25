package com.igorwojda.showcase.feature.team.data.repository

import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.data.datasource.api.model.InviteMemberRequestDto
import com.igorwojda.showcase.feature.team.data.datasource.api.service.TeamMemberRetrofitService
import com.igorwojda.showcase.feature.team.data.mapper.toDomain
import com.igorwojda.showcase.feature.team.data.mapper.toDomainResult
import com.igorwojda.showcase.feature.team.domain.model.InvitationCandidatePage
import com.igorwojda.showcase.feature.team.domain.model.MyInvitation
import com.igorwojda.showcase.feature.team.domain.model.TeamInvitation
import com.igorwojda.showcase.feature.team.domain.model.TeamMember
import com.igorwojda.showcase.feature.team.domain.repository.TeamMemberRepository

internal class TeamMemberRepositoryImpl(
    private val teamMemberRetrofitService: TeamMemberRetrofitService,
) : TeamMemberRepository {
    override suspend fun getMyInvitations(
        page: Int,
        pageSize: Int,
    ): Result<List<MyInvitation>> =
        dataResult {
            teamMemberRetrofitService
                .getMyInvitations(page = page, pageSize = pageSize)
                .toDomainResult { envelope -> envelope.data.map { it.toDomain() } }
        }

    override suspend fun acceptInvitation(invitationId: String): Result<TeamMember> =
        dataResult {
            teamMemberRetrofitService
                .acceptInvitation(invitationId = invitationId)
                .toDomainResult { it.data.toDomain() }
        }

    override suspend fun rejectInvitation(invitationId: String): Result<Unit> =
        dataResult {
            teamMemberRetrofitService
                .rejectInvitation(invitationId = invitationId)
                .toDomainResult { }
        }

    override suspend fun getTeamMembers(
        teamId: String,
        page: Int,
        pageSize: Int,
    ): Result<List<TeamMember>> =
        dataResult {
            teamMemberRetrofitService
                .getTeamMembers(teamId = teamId, page = page, pageSize = pageSize)
                .toDomainResult { envelope -> envelope.data.map { it.toDomain() } }
        }

    override suspend fun searchInvitationCandidates(
        teamId: String,
        query: String,
    ): Result<InvitationCandidatePage> =
        dataResult {
            teamMemberRetrofitService
                .searchInvitationCandidates(teamId = teamId, query = query)
                .toDomainResult { it.data.toDomain() }
        }

    override suspend fun inviteMember(
        teamId: String,
        userId: String,
    ): Result<TeamInvitation> =
        dataResult {
            teamMemberRetrofitService
                .inviteMember(teamId = teamId, body = InviteMemberRequestDto(userId = userId))
                .toDomainResult { it.data.toDomain() }
        }

    override suspend fun leaveTeam(teamId: String): Result<Unit> =
        dataResult {
            teamMemberRetrofitService
                .leaveTeam(teamId = teamId)
                .toDomainResult { }
        }
}
