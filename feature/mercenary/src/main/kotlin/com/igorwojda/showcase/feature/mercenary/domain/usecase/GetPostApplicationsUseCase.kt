package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryApplication
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository

/** Fetches all applications submitted to a specific mercenary recruitment post. */
class GetPostApplicationsUseCase(
    private val mercenaryRepository: MercenaryRepository,
) {
    suspend operator fun invoke(postId: String): Result<List<MercenaryApplication>> = mercenaryRepository.getPostApplications(postId)
}
