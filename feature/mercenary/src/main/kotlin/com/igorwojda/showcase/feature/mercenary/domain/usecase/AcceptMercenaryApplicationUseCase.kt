package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostDetail
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository

/** Approves a pending mercenary application on a recruitment post. */
class AcceptMercenaryApplicationUseCase(
    private val mercenaryRepository: MercenaryRepository,
) {
    suspend operator fun invoke(
        postId: String,
        appId: String,
    ): Result<MercenaryPostDetail> = mercenaryRepository.acceptApplication(postId, appId)
}
