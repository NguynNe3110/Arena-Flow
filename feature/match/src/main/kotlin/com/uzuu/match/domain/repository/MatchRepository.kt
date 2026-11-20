package com.uzuu.match.domain.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.MatchPage
import com.uzuu.match.domain.model.MatchDetail

internal interface MatchRepository {
    suspend fun getMatches(): Result<MatchPage>

    suspend fun getMatch(matchId: String): Result<MatchDetail>

    suspend fun createMatch(draft: com.uzuu.match.domain.model.MatchDraft): Result<com.uzuu.match.domain.model.MatchSummary>
}
