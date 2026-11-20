package com.uzuu.pairing.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.pairing.domain.model.PairingRoom
import com.uzuu.pairing.domain.repository.PairingRepository

internal class ConfirmPackageUseCase(
    private val pairingRepository: PairingRepository,
) {
    suspend operator fun invoke(pairingId: String): Result<PairingRoom> = pairingRepository.confirmPackage(pairingId)
}
