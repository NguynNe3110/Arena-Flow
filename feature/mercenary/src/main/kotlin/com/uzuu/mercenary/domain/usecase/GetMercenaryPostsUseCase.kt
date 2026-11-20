package com.uzuu.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.mercenary.domain.model.MercenaryPost
import com.uzuu.mercenary.domain.repository.MercenaryRepository

internal class GetMercenaryPostsUseCase(
    private val mercenaryRepository: MercenaryRepository,
) {
    suspend operator fun invoke(): Result<List<MercenaryPost>> = mercenaryRepository.getMercenaryPosts()
}
