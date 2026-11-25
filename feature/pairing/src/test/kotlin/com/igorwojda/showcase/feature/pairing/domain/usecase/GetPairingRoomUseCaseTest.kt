package com.igorwojda.showcase.feature.pairing.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.pairing.PairingTestFixtures
import com.igorwojda.showcase.feature.pairing.domain.repository.PairingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetPairingRoomUseCaseTest {
    private val pairingRepository: PairingRepository = mockk()
    private val sut = GetPairingRoomUseCase(pairingRepository)

    @Test
    fun `when invoked then returns pairing room from repository`() =
        runTest {
            val expected = Result.Success(PairingTestFixtures.sampleRoom())
            coEvery { pairingRepository.getPairingRoom("pairing-beta-alpha") } returns expected

            val actual = sut("pairing-beta-alpha")

            actual shouldBeEqualTo expected
        }
}
