@file:Suppress("ComplexInterface")

package com.igorwojda.showcase.feature.match.domain.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.AcceptRequestResult
import com.igorwojda.showcase.feature.match.domain.model.CancelMatchResult
import com.igorwojda.showcase.feature.match.domain.model.MatchCostAgreement
import com.igorwojda.showcase.feature.match.domain.model.MatchDetail
import com.igorwojda.showcase.feature.match.domain.model.MatchDraft
import com.igorwojda.showcase.feature.match.domain.model.MatchFilter
import com.igorwojda.showcase.feature.match.domain.model.MatchPage
import com.igorwojda.showcase.feature.match.domain.model.MatchRequest
import com.igorwojda.showcase.feature.match.domain.model.SportOption
import com.igorwojda.showcase.feature.match.domain.model.WithdrawRequestResult

/** Repository contract for match discovery, lifecycle, and join requests. */
@Suppress("TooManyFunctions")
interface MatchRepository {
    suspend fun getMatches(filter: MatchFilter): Result<MatchPage>

    suspend fun getMyMatches(
        teamId: String?,
        status: String?,
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<MatchPage>

    suspend fun getMatchDetail(matchId: String): Result<MatchDetail>

    suspend fun createMatch(draft: MatchDraft): Result<MatchDetail>

    suspend fun updateMatch(
        matchId: String,
        draft: MatchDraft,
    ): Result<MatchDetail>

    suspend fun updateCostAgreement(
        matchId: String,
        costAgreement: MatchCostAgreement,
    ): Result<MatchCostAgreement>

    suspend fun cancelMatch(
        matchId: String,
        teamId: String,
        expectedStatus: String,
        reason: String?,
    ): Result<CancelMatchResult>

    suspend fun forfeitMatch(
        matchId: String,
        teamId: String,
        reason: String?,
    ): Result<Unit>

    suspend fun getMatchRequests(
        matchId: String,
        status: String? = null,
    ): Result<List<MatchRequest>>

    suspend fun sendMatchRequest(
        matchId: String,
        teamId: String,
        message: String? = null,
    ): Result<MatchRequest>

    suspend fun acceptMatchRequest(requestId: String): Result<AcceptRequestResult>

    suspend fun rejectMatchRequest(
        requestId: String,
        reason: String? = null,
    ): Result<Unit>

    suspend fun withdrawMatchRequest(
        requestId: String,
        expectedStatus: String,
        reason: String? = null,
    ): Result<WithdrawRequestResult>

    suspend fun getSports(): Result<List<SportOption>>
}
