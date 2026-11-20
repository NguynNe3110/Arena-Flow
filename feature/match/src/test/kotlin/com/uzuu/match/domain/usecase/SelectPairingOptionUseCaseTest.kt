package com.uzuu.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.PairingRoom
import com.uzuu.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SelectPairingOptionUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = SelectPairingOptionUseCase(matchRepository)

    @Test
    fun `invoke calls repository updatePackageOption`() =
        runTest {
            val room = mockk<PairingRoom>()
            val expected = Result.Success(room)
            coEvery { matchRepository.updatePackageOption("pr1", "v1", "t1") } returns expected

            val actual = sut("pr1", "v1", "t1")

            assertEquals(expected, actual)
        }
}
