package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository

/** Closes an open or filled mercenary recruitment post. */
class CloseMercenaryPostUseCase(
    private val mercenaryRepository: MercenaryRepository,
) {
    suspend operator fun invoke(postId: String): Result<Unit> = mercenaryRepository.closeMercenaryPost(postId)
}
