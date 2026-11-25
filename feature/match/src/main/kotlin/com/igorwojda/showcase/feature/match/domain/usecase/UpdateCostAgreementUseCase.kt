package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.MatchCostAgreement
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository

/** Updates the cost agreement for a flow A (HAS_VENUE) open match. */
class UpdateCostAgreementUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(
        matchId: String,
        costAgreement: MatchCostAgreement,
    ): Result<MatchCostAgreement> = matchRepository.updateCostAgreement(matchId = matchId, costAgreement = costAgreement)
}
