package com.igorwojda.showcase.feature.pairing.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.pairing.PairingTestFixtures
import com.igorwojda.showcase.feature.pairing.domain.repository.PairingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class AddPairingOptionUseCaseTest {
    private val pairingRepository: PairingRepository = mockk()
    private val sut = AddPairingOptionUseCase(pairingRepository)

    @Test
    fun `when invoked then adds option via repository`() =
        runTest {
            val input = PairingTestFixtures.sampleAddOptionInput()
            val expected = Result.Success(PairingTestFixtures.sampleRoom(revision = 2))
            coEvery { pairingRepository.addPairingOption("pairing-beta-alpha", input) } returns expected

            val actual = sut("pairing-beta-alpha", input)

            actual shouldBeEqualTo expected
        }
}
