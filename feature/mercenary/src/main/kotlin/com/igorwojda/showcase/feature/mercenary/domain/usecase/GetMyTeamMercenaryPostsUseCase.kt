package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPage
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository

/** Fetches paginated mercenary recruitment posts created by the current captain's team. */
class GetMyTeamMercenaryPostsUseCase(
    private val mercenaryRepository: MercenaryRepository,
) {
    suspend operator fun invoke(
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<MercenaryPage> = mercenaryRepository.getMyTeamPosts(page = page, pageSize = pageSize)
}
