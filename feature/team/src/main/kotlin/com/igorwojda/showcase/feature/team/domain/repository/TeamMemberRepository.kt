package com.igorwojda.showcase.feature.team.domain.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.InvitationCandidatePage
import com.igorwojda.showcase.feature.team.domain.model.MyInvitation
import com.igorwojda.showcase.feature.team.domain.model.TeamInvitation
import com.igorwojda.showcase.feature.team.domain.model.TeamMember

internal interface TeamMemberRepository {
    suspend fun getMyInvitations(
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<List<MyInvitation>>

    suspend fun acceptInvitation(invitationId: String): Result<TeamMember>

    suspend fun rejectInvitation(invitationId: String): Result<Unit>

    suspend fun getTeamMembers(
        teamId: String,
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<List<TeamMember>>

    suspend fun searchInvitationCandidates(
        teamId: String,
        query: String,
    ): Result<InvitationCandidatePage>

    suspend fun inviteMember(
        teamId: String,
        userId: String,
    ): Result<TeamInvitation>

    suspend fun leaveTeam(teamId: String): Result<Unit>
}
