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

class CancelPairingUseCaseTest {
    private val pairingRepository: PairingRepository = mockk()
    private val sut = CancelPairingUseCase(pairingRepository)

    @Test
    fun `when invoked then cancels pairing via repository`() =
        runTest {
            val expected =
                Result.Success(
                    PairingTestFixtures.sampleRoom(status = PairingStatus.CANCELLED).copy(
                        cancelReason = "Đội không đủ người",
                    ),
                )
            coEvery { pairingRepository.cancelPairing("pairing-beta-alpha", "Đội không đủ người") } returns expected

            val actual = sut("pairing-beta-alpha", "Đội không đủ người")

            actual shouldBeEqualTo expected
        }
}
