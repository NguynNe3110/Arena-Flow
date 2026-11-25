package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostDetail
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository

/** Fetches full details for a single mercenary recruitment post. */
class GetMercenaryPostDetailUseCase(
    private val mercenaryRepository: MercenaryRepository,
) {
    suspend operator fun invoke(postId: String): Result<MercenaryPostDetail> = mercenaryRepository.getMercenaryPostDetail(postId)
}
