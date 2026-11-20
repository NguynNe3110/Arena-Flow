package com.uzuu.pairing.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.pairing.domain.model.PairingRoom
import com.uzuu.pairing.domain.repository.PairingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetPairingRoomUseCaseTest {
    private val pairingRepository: PairingRepository = mockk()
    private val sut = GetPairingRoomUseCase(pairingRepository)

    @Test
    fun `invoke calls repository getPairingRoom`() =
        runTest {
            val room = mockk<PairingRoom>()
            val expected = Result.Success(room)
            coEvery { pairingRepository.getPairingRoom("pr1") } returns expected

            val actual = sut("pr1")

            assertEquals(expected, actual)
        }
}
