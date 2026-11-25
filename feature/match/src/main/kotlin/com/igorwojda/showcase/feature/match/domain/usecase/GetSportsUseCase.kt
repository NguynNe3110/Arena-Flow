package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.SportOption
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository

/** Fetches active sports available for match creation and filtering. */
class GetSportsUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(): Result<List<SportOption>> = matchRepository.getSports()
}
