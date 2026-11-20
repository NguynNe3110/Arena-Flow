package com.uzuu.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.MercenaryPost
import com.uzuu.match.domain.repository.MatchRepository

internal class GetMercenaryPostsUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(): Result<List<MercenaryPost>> =
        matchRepository.getMercenaryPosts()
}
