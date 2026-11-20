package com.uzuu.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.mercenary.domain.model.MercenaryPost
import com.uzuu.mercenary.domain.repository.MercenaryRepository

internal class ApplyMercenaryUseCase(
    private val mercenaryRepository: MercenaryRepository,
) {
    suspend operator fun invoke(
        postId: String,
        message: String,
    ): Result<MercenaryPost> = mercenaryRepository.applyMercenary(postId, message)
}
