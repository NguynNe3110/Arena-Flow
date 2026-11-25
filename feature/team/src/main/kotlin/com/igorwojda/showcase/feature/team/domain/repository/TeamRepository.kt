package com.igorwojda.showcase.feature.team.domain.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.CaptainHistoryEntry
import com.igorwojda.showcase.feature.team.domain.model.CaptainTransfer
import com.igorwojda.showcase.feature.team.domain.model.MyTeam
import com.igorwojda.showcase.feature.team.domain.model.TeamDetail
import com.igorwojda.showcase.feature.team.domain.model.TeamDraft
import com.igorwojda.showcase.feature.team.domain.model.TeamSummary

internal interface TeamRepository {
    suspend fun createTeam(draft: TeamDraft): Result<TeamDetail>

    suspend fun getTeams(
        status: String = "ACTIVE",
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<List<TeamSummary>>

    suspend fun getTeamDetail(teamId: String): Result<TeamDetail>

    suspend fun updateTeam(
        teamId: String,
        draft: TeamDraft,
    ): Result<TeamDetail>

    suspend fun disbandTeam(
        teamId: String,
        reason: String?,
    ): Result<TeamDetail>

    suspend fun getMyTeams(
        role: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<List<MyTeam>>

    suspend fun transferCaptain(
        teamId: String,
        newCaptainId: String,
        reason: String?,
    ): Result<CaptainTransfer>

    suspend fun getCaptainHistory(
        teamId: String,
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<List<CaptainHistoryEntry>>
}
