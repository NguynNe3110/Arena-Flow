package com.uzuu.match.presentation.screen.matchdetail

import androidx.lifecycle.SavedStateHandle
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import com.uzuu.match.domain.model.MatchDetail
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.domain.usecase.GetMatchDetailUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class MatchDetailViewModelTest {
    @Test
    fun `request survives recreation and a duplicate does not replace its team`() =
        runTest {
            val useCase = mockk<GetMatchDetailUseCase>()
            coEvery { useCase("id") } returns Result.Success(detail())
            val handle = SavedStateHandle()
            val viewModel = MatchDetailViewModel(useCase, handle)
            viewModel.load("id")
            advanceUntilIdle()
            viewModel.sendDemoRequest("Rồng Vàng FC")
            viewModel.sendDemoRequest("Phượng Hoàng FC")
            val restored = MatchDetailViewModel(useCase, handle)
            restored.load("id")
            advanceUntilIdle()
            assertEquals("Rồng Vàng FC", restored.uiState.value.requestTeam)
            restored.withdrawDemoRequest()
            assertNull(restored.uiState.value.requestTeam)
        }

    @Test
    fun `cannot withdraw a pairing or request a closed match`() =
        runTest {
            val useCase = mockk<GetMatchDetailUseCase>()
            coEvery { useCase("b") } returns Result.Success(detail(MatchType.NEED_VENUE))
            coEvery { useCase("closed") } returns Result.Success(detail(status = "CONFIRMED"))
            val viewModel = MatchDetailViewModel(useCase, SavedStateHandle())
            viewModel.load("b")
            advanceUntilIdle()
            viewModel.sendDemoRequest("Rồng Vàng FC")
            viewModel.withdrawDemoRequest()
            assertEquals("Rồng Vàng FC", viewModel.uiState.value.requestTeam)
            viewModel.load("closed")
            advanceUntilIdle()
            viewModel.sendDemoRequest("Rồng Vàng FC")
            assertNull(viewModel.uiState.value.requestTeam)
        }

    @Test
    fun `retry loads the requested detail after a network failure`() =
        runTest {
            val useCase = mockk<GetMatchDetailUseCase>()
            coEvery { useCase("id") } returnsMany listOf(Result.Failure(AppError.Network), Result.Success(detail()))
            val viewModel = MatchDetailViewModel(useCase, SavedStateHandle())
            viewModel.load("id")
            advanceUntilIdle()
            assertEquals(AppError.Network, viewModel.uiState.value.error)
            viewModel.load("id")
            advanceUntilIdle()
            assertEquals(
                "id",
                viewModel.uiState.value.match
                    ?.matchId,
            )
            assertNull(viewModel.uiState.value.error)
        }

    private fun detail(
        type: MatchType = MatchType.HAS_VENUE,
        status: String = "OPEN",
    ) = MatchDetail(MatchSummary("id", type, "Bình Minh FC", null, status = status))
}
