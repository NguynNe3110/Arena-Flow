package com.uzuu.team.data.datasource.api

import com.uzuu.team.domain.model.TeamDetail
import com.uzuu.team.domain.model.TeamDraft
import com.uzuu.team.domain.model.TeamMember

interface TeamRemoteDataSource {
    suspend fun getMyTeam(): TeamDetail

    suspend fun createTeam(draft: TeamDraft): TeamDetail

    suspend fun inviteTeamMember(
        teamId: String,
        name: String,
        position: String,
    ): TeamMember
}
