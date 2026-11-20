package com.uzuu.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.MercenaryPost
import com.uzuu.match.domain.repository.MatchRepository

internal class ApplyMercenaryUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(
        postId: String,
        message: String,
    ): Result<MercenaryPost> = matchRepository.applyMercenary(postId, message)
}
