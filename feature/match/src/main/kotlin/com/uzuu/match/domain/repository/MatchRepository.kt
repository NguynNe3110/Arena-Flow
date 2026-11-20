package com.uzuu.match.domain.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.MatchDetail
import com.uzuu.match.domain.model.MatchDraft
import com.uzuu.match.domain.model.MatchPage
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MercenaryPost
import com.uzuu.match.domain.model.PairingRoom
import com.uzuu.match.domain.model.TeamDetail
import com.uzuu.match.domain.model.TeamDraft
import com.uzuu.match.domain.model.TeamMember

internal interface MatchRepository {
    suspend fun getMatches(): Result<MatchPage>

    suspend fun getMatch(matchId: String): Result<MatchDetail>

    suspend fun createMatch(draft: MatchDraft): Result<MatchSummary>

    suspend fun getPairingRoom(pairingId: String): Result<PairingRoom>

    suspend fun confirmPackage(pairingId: String): Result<PairingRoom>

    suspend fun updatePackageOption(
        pairingId: String,
        venueId: String?,
        timeSlot: String?,
    ): Result<PairingRoom>

    suspend fun getMercenaryPosts(): Result<List<MercenaryPost>>

    suspend fun applyMercenary(
        postId: String,
        message: String,
    ): Result<MercenaryPost>

    suspend fun getMyTeam(): Result<TeamDetail>

    suspend fun createTeam(draft: TeamDraft): Result<TeamDetail>

    suspend fun inviteTeamMember(
        teamId: String,
        name: String,
        position: String,
    ): Result<TeamMember>
}
