package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository

/** Rejects a pending mercenary application on a recruitment post. */
class RejectMercenaryApplicationUseCase(
    private val mercenaryRepository: MercenaryRepository,
) {
    suspend operator fun invoke(
        postId: String,
        appId: String,
    ): Result<Unit> = mercenaryRepository.rejectApplication(postId, appId)
}
