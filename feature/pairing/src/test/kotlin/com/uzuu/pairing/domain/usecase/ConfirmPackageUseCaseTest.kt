package com.uzuu.pairing.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.pairing.domain.model.PairingRoom
import com.uzuu.pairing.domain.repository.PairingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConfirmPackageUseCaseTest {
    private val pairingRepository: PairingRepository = mockk()
    private val sut = ConfirmPackageUseCase(pairingRepository)

    @Test
    fun `invoke calls repository confirmPackage`() =
        runTest {
            val room = mockk<PairingRoom>()
            val expected = Result.Success(room)
            coEvery { pairingRepository.confirmPackage("pr1") } returns expected

            val actual = sut("pr1")

            assertEquals(expected, actual)
        }
}
