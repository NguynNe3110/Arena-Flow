package com.igorwojda.showcase.feature.reputation.presentation.screen.postmatchreport

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.domain.model.MatchRating
import com.igorwojda.showcase.feature.reputation.domain.model.MatchRatings
import com.igorwojda.showcase.feature.reputation.domain.model.PostMatchOutcome
import com.igorwojda.showcase.feature.reputation.domain.usecase.GetMatchRatingsUseCase
import com.igorwojda.showcase.feature.reputation.domain.usecase.SubmitMatchRatingUseCase
import com.igorwojda.showcase.feature.reputation.domain.usecase.SubmitMatchReportUseCase
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class PostMatchReportViewModelTest {
    private val getMatchRatingsUseCase: GetMatchRatingsUseCase = mockk()
    private val submitMatchRatingUseCase: SubmitMatchRatingUseCase = mockk()
    private val submitMatchReportUseCase: SubmitMatchReportUseCase = mockk()
    private val sut =
        PostMatchReportViewModel(
            getMatchRatingsUseCase = getMatchRatingsUseCase,
            submitMatchRatingUseCase = submitMatchRatingUseCase,
            submitMatchReportUseCase = submitMatchReportUseCase,
        )

    @Test
    fun `load fetches existing match ratings`() =
        runTest {
            val ratings = MatchRatings(matchId = "m1", teamId = "t1", given = null, received = null)
            coEvery { getMatchRatingsUseCase("m1", "t1") } returns Result.Success(ratings)

            sut.onEvent(PostMatchReportEvent.Load("m1", "t1"))
            advanceUntilIdle()

            assertEquals(ratings, sut.uiStateFlow.value.existingRatings)
            assertFalse(sut.uiStateFlow.value.isLoading)
        }

    @Test
    fun `submit rating succeeds for completed match`() =
        runTest {
            val ratings = MatchRatings(matchId = "m1", teamId = "t1", given = null, received = null)
            val created =
                MatchRating(
                    ratingId = "r1",
                    matchId = "m1",
                    raterTeamId = "t1",
                    rateeTeamId = "t2",
                    isNoShow = false,
                    score = 5,
                    comment = "Đá đẹp",
                    raterTeamScore = 3,
                    rateeTeamScore = 2,
                    createdAt = "2026-11-18T20:00:00",
                    matchStatus = "COMPLETED",
                )
            coEvery { getMatchRatingsUseCase("m1", "t1") } returns Result.Success(ratings)
            coEvery { submitMatchRatingUseCase(any()) } returns Result.Success(created)

            sut.onEvent(PostMatchReportEvent.Load("m1", "t1"))
            advanceUntilIdle()
            sut.onEvent(PostMatchReportEvent.SelectOutcome(PostMatchOutcome.COMPLETED))
            sut.onEvent(PostMatchReportEvent.SelectStarRating(5))
            sut.onEvent(PostMatchReportEvent.ToggleFairPlayTag("Đá đẹp"))
            sut.onEvent(PostMatchReportEvent.Submit)
            advanceUntilIdle()

            assertNotNull(sut.uiStateFlow.value.submittedRating)
            assertEquals(
                5,
                sut.uiStateFlow.value.submittedRating
                    ?.score,
            )
            assertFalse(sut.uiStateFlow.value.isSubmitting)
        }
}
