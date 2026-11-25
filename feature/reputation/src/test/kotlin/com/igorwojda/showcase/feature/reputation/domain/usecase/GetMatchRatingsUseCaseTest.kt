package com.igorwojda.showcase.feature.reputation.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.domain.model.MatchRatings
import com.igorwojda.showcase.feature.reputation.domain.repository.ReputationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetMatchRatingsUseCaseTest {
    private val reputationRepository: ReputationRepository = mockk()
    private val sut = GetMatchRatingsUseCase(reputationRepository)

    @Test
    fun `invoke returns match ratings from repository`() =
        runTest {
            val expected = MatchRatings(matchId = "m1", teamId = "t1", given = null, received = null)
            coEvery { reputationRepository.getMatchRatings("m1", "t1") } returns Result.Success(expected)

            val actual = sut(matchId = "m1", teamId = "t1")

            assertEquals(Result.Success(expected), actual)
        }
}
