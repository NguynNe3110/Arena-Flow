package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.model.CreateMercenaryPostParams
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostDetail
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository

/** Creates a new mercenary recruitment post for a team. */
class CreateMercenaryPostUseCase(
    private val mercenaryRepository: MercenaryRepository,
) {
    suspend operator fun invoke(params: CreateMercenaryPostParams): Result<MercenaryPostDetail> =
        mercenaryRepository.createMercenaryPost(params)
}
