package com.uzuu.match.presentation.screen.matchlist

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import com.uzuu.match.domain.model.MatchPage
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.domain.usecase.GetMatchUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class MatchListViewModelTest {
    private val getMatchUseCase = mockk<GetMatchUseCase>()
    private lateinit var sut: MatchListViewModel

    @Test
    fun `init loads matches successfully`() = runTest {
        val matches = listOf(
            MatchSummary(
                matchId = "m1",
                type = MatchType.HAS_VENUE,
                authorTeamName = "Bình Minh FC",
                venueName = "Sân An Phú",
            ),
        )
        val matchPage = MatchPage(
            matches = matches,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            totalItems = 1,
        )
        coEvery { getMatchUseCase() } returns Result.Success(matchPage)

        sut = MatchListViewModel(getMatchUseCase)
        advanceUntilIdle()

        assertEquals(matches, sut.uiStateFlow.value.matches)
        assertEquals(1, sut.uiStateFlow.value.totalItems)
        assertFalse(sut.uiStateFlow.value.isLoading)
        assertNull(sut.uiStateFlow.value.error)
    }

    @Test
    fun `init emits error on failure`() = runTest {
        coEvery { getMatchUseCase() } returns Result.Failure(AppError.Network)

        sut = MatchListViewModel(getMatchUseCase)
        advanceUntilIdle()

        assertEquals(emptyList<MatchSummary>(), sut.uiStateFlow.value.matches)
        assertEquals(AppError.Network, sut.uiStateFlow.value.error)
        assertFalse(sut.uiStateFlow.value.isLoading)
    }

    @Test
    fun `onRefresh reloads matches`() = runTest {
        val matchPage = MatchPage(
            matches = emptyList(),
            page = 1,
            pageSize = 20,
            totalPages = 0,
            totalItems = 0,
        )
        coEvery { getMatchUseCase() } returns Result.Success(matchPage)

        sut = MatchListViewModel(getMatchUseCase)
        advanceUntilIdle()

        sut.onRefresh()
        advanceUntilIdle()

        coVerify(exactly = 2) { getMatchUseCase() }
    }
}
