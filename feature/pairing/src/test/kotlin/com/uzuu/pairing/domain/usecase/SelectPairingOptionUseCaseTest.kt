package com.uzuu.pairing.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.pairing.domain.model.PairingRoom
import com.uzuu.pairing.domain.repository.PairingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SelectPairingOptionUseCaseTest {
    private val pairingRepository: PairingRepository = mockk()
    private val sut = SelectPairingOptionUseCase(pairingRepository)

    @Test
    fun `invoke calls repository updatePackageOption`() =
        runTest {
            val room = mockk<PairingRoom>()
            val expected = Result.Success(room)
            coEvery { pairingRepository.updatePackageOption("pr1", "v1", "t1") } returns expected

            val actual = sut("pr1", "v1", "t1")

            assertEquals(expected, actual)
        }
}
