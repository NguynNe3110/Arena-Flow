package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPage
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostFilter
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository

/** Fetches paginated open mercenary recruitment posts matching the given filter. */
class GetMercenaryPostsUseCase(
    private val mercenaryRepository: MercenaryRepository,
) {
    suspend operator fun invoke(filter: MercenaryPostFilter = MercenaryPostFilter()): Result<MercenaryPage> =
        mercenaryRepository.getMercenaryPosts(filter)
}
