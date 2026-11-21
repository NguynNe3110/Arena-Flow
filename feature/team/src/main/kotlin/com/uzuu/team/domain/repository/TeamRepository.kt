package com.uzuu.team.domain.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.team.domain.model.TeamDetail
import com.uzuu.team.domain.model.TeamDraft
import com.uzuu.team.domain.model.TeamMember

interface TeamRepository {
    suspend fun getMyTeam(): Result<TeamDetail>

    suspend fun createTeam(draft: TeamDraft): Result<TeamDetail>

    suspend fun inviteTeamMember(
        teamId: String,
        name: String,
        position: String,
    ): Result<TeamMember>
}
