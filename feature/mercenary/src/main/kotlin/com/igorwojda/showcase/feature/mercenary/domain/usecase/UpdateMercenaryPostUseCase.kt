package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostDetail
import com.igorwojda.showcase.feature.mercenary.domain.model.UpdateMercenaryPostParams
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository

/** Updates an existing mercenary recruitment post. */
class UpdateMercenaryPostUseCase(
    private val mercenaryRepository: MercenaryRepository,
) {
    suspend operator fun invoke(params: UpdateMercenaryPostParams): Result<MercenaryPostDetail> =
        mercenaryRepository.updateMercenaryPost(params)
}
