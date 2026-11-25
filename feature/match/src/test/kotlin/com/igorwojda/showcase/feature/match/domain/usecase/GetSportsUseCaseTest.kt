package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.SportOption
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetSportsUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = GetSportsUseCase(matchRepository)

    @Test
    fun `when invoked then returns active sports`() =
        runTest {
            val expected = Result.Success(listOf(SportOption("sport-football", "Bóng đá", true)))
            coEvery { matchRepository.getSports() } returns expected

            val actual = sut()

            actual shouldBeEqualTo expected
        }
}
