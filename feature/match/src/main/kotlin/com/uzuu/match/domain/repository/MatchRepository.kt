package com.uzuu.match.domain.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.MatchPage

internal interface MatchRepository {
    suspend fun getMatches(): Result<MatchPage>
}
