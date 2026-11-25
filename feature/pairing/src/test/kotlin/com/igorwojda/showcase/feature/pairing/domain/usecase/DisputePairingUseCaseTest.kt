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

class DisputePairingUseCaseTest {
    private val pairingRepository: PairingRepository = mockk()
    private val sut = DisputePairingUseCase(pairingRepository)

    @Test
    fun `when invoked then disputes pairing via repository`() =
        runTest {
            val expected =
                Result.Success(
                    PairingTestFixtures.sampleRoom(status = PairingStatus.DISPUTED).copy(
                        disputeReason = "Thay đổi giá sân",
                    ),
                )
            coEvery { pairingRepository.disputePairing("pairing-beta-alpha", "Thay đổi giá sân") } returns expected

            val actual = sut("pairing-beta-alpha", "Thay đổi giá sân")

            actual shouldBeEqualTo expected
        }
}
