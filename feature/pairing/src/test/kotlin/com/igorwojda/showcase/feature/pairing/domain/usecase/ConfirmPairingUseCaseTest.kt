package com.igorwojda.showcase.feature.pairing.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.pairing.PairingTestFixtures
import com.igorwojda.showcase.feature.pairing.domain.model.PairingStatus
import com.igorwojda.showcase.feature.pairing.domain.repository.PairingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ConfirmPairingUseCaseTest {
    private val pairingRepository: PairingRepository = mockk()
    private val sut = ConfirmPairingUseCase(pairingRepository)

    @Test
    fun `when invoked then confirms pairing via repository`() =
        runTest {
            val expected =
                Result.Success(
                    PairingTestFixtures.sampleRoom(
                        status = PairingStatus.CONFIRMED,
                        hostConfirmed = true,
                        guestConfirmed = true,
                    ),
                )
            coEvery { pairingRepository.confirmPairing("pairing-beta-alpha", 1, "opt-1") } returns expected

            val actual = sut("pairing-beta-alpha", 1, "opt-1")

            actual shouldBeEqualTo expected
        }
}
