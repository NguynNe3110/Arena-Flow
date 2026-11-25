package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository

/** Withdraws the current player's mercenary application from a recruitment post. */
class WithdrawMercenaryApplicationUseCase(
    private val mercenaryRepository: MercenaryRepository,
) {
    suspend operator fun invoke(
        postId: String,
        appId: String,
    ): Result<Unit> = mercenaryRepository.withdrawApplication(postId, appId)
}
