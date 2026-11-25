package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryApplication
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository

/** Submits a player's application to a mercenary recruitment post. */
class ApplyMercenaryUseCase(
    private val mercenaryRepository: MercenaryRepository,
) {
    suspend operator fun invoke(
        postId: String,
        position: PlayerPosition,
        note: String = "",
    ): Result<MercenaryApplication> = mercenaryRepository.applyToMercenaryPost(postId, position, note)
}
