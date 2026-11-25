package com.igorwojda.showcase.feature.reputation.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.domain.model.MatchRating
import com.igorwojda.showcase.feature.reputation.domain.model.SubmitRatingParams
import com.igorwojda.showcase.feature.reputation.domain.repository.ReputationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SubmitMatchRatingUseCaseTest {
    private val reputationRepository: ReputationRepository = mockk()
    private val sut = SubmitMatchRatingUseCase(reputationRepository)

    @Test
    fun `invoke submits rating and returns result`() =
        runTest {
            val params = SubmitRatingParams(matchId = "m1", isNoShow = false, score = 5)
            val expected =
                MatchRating(
                    ratingId = "r1",
                    matchId = "m1",
                    raterTeamId = "t1",
                    rateeTeamId = "t2",
                    isNoShow = false,
                    score = 5,
                    comment = null,
                    raterTeamScore = null,
                    rateeTeamScore = null,
                    createdAt = "2026-11-18T20:00:00",
                    matchStatus = "COMPLETED",
                )
            coEvery { reputationRepository.submitMatchRating(params) } returns Result.Success(expected)

            val actual = sut(params)

            assertEquals(Result.Success(expected), actual)
        }
}
