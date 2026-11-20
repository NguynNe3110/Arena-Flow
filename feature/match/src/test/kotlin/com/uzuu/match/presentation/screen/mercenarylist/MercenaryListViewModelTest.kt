package com.uzuu.match.presentation.screen.mercenarylist

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import com.uzuu.match.domain.model.MercenaryPost
import com.uzuu.match.domain.usecase.ApplyMercenaryUseCase
import com.uzuu.match.domain.usecase.GetMercenaryPostsUseCase
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
class MercenaryListViewModelTest {
    private val getMercenaryPostsUseCase: GetMercenaryPostsUseCase = mockk()
    private val applyMercenaryUseCase: ApplyMercenaryUseCase = mockk()

    @Test
    fun `init loads mercenary posts`() = runTest {
        val posts = listOf(samplePost("rec-1"))
        coEvery { getMercenaryPostsUseCase() } returns Result.Success(posts)

        val viewModel = MercenaryListViewModel(getMercenaryPostsUseCase, applyMercenaryUseCase)
        advanceUntilIdle()

        assertEquals(posts, viewModel.uiState.value.posts)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `filter position and radius update state`() = runTest {
        coEvery { getMercenaryPostsUseCase() } returns Result.Success(emptyList())

        val viewModel = MercenaryListViewModel(getMercenaryPostsUseCase, applyMercenaryUseCase)
        advanceUntilIdle()

        viewModel.setFilterPosition("GK")
        assertEquals("GK", viewModel.uiState.value.filterPosition)

        viewModel.setRadius(5.5f)
        assertEquals(5.5f, viewModel.uiState.value.radiusKm)
    }

    @Test
    fun `applyToPost marks post as applied`() = runTest {
        val post = samplePost("rec-1", isApplied = false)
        val applied = post.copy(isApplied = true, pendingApplicants = 3)
        coEvery { getMercenaryPostsUseCase() } returns Result.Success(listOf(post))
        coEvery { applyMercenaryUseCase("rec-1", "msg") } returns Result.Success(applied)

        val viewModel = MercenaryListViewModel(getMercenaryPostsUseCase, applyMercenaryUseCase)
        advanceUntilIdle()

        viewModel.applyToPost("rec-1", "msg")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.posts.first().isApplied)
        assertEquals(3, viewModel.uiState.value.posts.first().pendingApplicants)
        assertFalse(viewModel.uiState.value.isApplying)
    }

    private fun samplePost(id: String, isApplied: Boolean = false) = MercenaryPost(
        id = id,
        teamName = "Bình Minh FC",
        captainName = "Lê Hoàng",
        position = "GK",
        positionLabel = "[CẦN 1 THỦ MÔN - GK]",
        matchDate = "21/11/2026",
        timeSlot = "19:00 - 20:30",
        estimatedArea = "TP. Thủ Đức",
        distanceKm = 2.5,
        costShare = "Bao sân",
        requirements = "Bắt an toàn",
        isApplied = isApplied,
    )
}
