package com.uzuu.match.presentation.screen.matchreview

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import com.uzuu.match.domain.model.MatchDraft
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.domain.usecase.CreateMatchUseCase
import com.uzuu.match.presentation.screen.matchcreate.MatchDraftHolder
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class MatchReviewViewModelTest {
    private val createMatchUseCase: CreateMatchUseCase = mockk()

    @Test
    fun `confirmPost succeeds and resets draft`() = runTest {
        val holder = MatchDraftHolder()
        holder.updateDraft(MatchDraft(hostTeamName = "My Team"))
        val summary = MatchSummary(
            matchId = "id-1",
            type = MatchType.HAS_VENUE,
            authorTeamName = "My Team",
            venueName = "Stadium",
        )
        coEvery { createMatchUseCase(any()) } returns Result.Success(summary)

        val viewModel = MatchReviewViewModel(holder, createMatchUseCase)
        advanceUntilIdle()

        var posted = false
        viewModel.confirmPost { posted = true }
        advanceUntilIdle()

        assertTrue(posted)
        assertEquals(summary, viewModel.uiState.value.postedMatch)
        assertFalse(viewModel.uiState.value.isPosting)
    }

    @Test
    fun `confirmPost emits error on failure`() = runTest {
        val holder = MatchDraftHolder()
        coEvery { createMatchUseCase(any()) } returns Result.Failure(AppError.Network)

        val viewModel = MatchReviewViewModel(holder, createMatchUseCase)
        advanceUntilIdle()

        var posted = false
        viewModel.confirmPost { posted = true }
        advanceUntilIdle()

        assertFalse(posted)
        assertEquals(AppError.Network, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isPosting)
    }
}
