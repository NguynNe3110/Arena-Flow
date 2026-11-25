package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryApplication
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository

/** Fetches all mercenary applications submitted by the current user. */
class GetMyMercenaryApplicationsUseCase(
    private val mercenaryRepository: MercenaryRepository,
) {
    suspend operator fun invoke(): Result<List<MercenaryApplication>> = mercenaryRepository.getMyApplications()
}
