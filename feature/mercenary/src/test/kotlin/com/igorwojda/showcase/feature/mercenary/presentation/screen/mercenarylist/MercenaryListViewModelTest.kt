package com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarylist

import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.model.SkillLevel
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPage
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostStatus
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostSummary
import com.igorwojda.showcase.feature.mercenary.domain.usecase.ApplyMercenaryUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetMercenaryPostsUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetMyMercenaryApplicationsUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetMyTeamMercenaryPostsUseCase
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class MercenaryListViewModelTest {
    private val applyMercenaryUseCase: ApplyMercenaryUseCase = mockk()
    private val getMercenaryPostsUseCase: GetMercenaryPostsUseCase = mockk()
    private val getMyMercenaryApplicationsUseCase: GetMyMercenaryApplicationsUseCase = mockk()
    private val getMyTeamMercenaryPostsUseCase: GetMyTeamMercenaryPostsUseCase = mockk()
    private val sessionRepository: SessionRepository = mockk()
    private lateinit var sut: MercenaryListViewModel

    @Test
    fun `init loads explore posts`() =
        runTest {
            val post = sampleSummary("rec-1")
            every { sessionRepository.getWards() } returns emptyList()
            every { sessionRepository.observeSession() } returns flowOf(ArenaSessionState())
            coEvery { getMercenaryPostsUseCase(any()) } returns
                Result.Success(MercenaryPage(listOf(post), 1, 20, 1, 1))

            sut =
                MercenaryListViewModel(
                    applyMercenaryUseCase = applyMercenaryUseCase,
                    getMercenaryPostsUseCase = getMercenaryPostsUseCase,
                    getMyMercenaryApplicationsUseCase = getMyMercenaryApplicationsUseCase,
                    getMyTeamMercenaryPostsUseCase = getMyTeamMercenaryPostsUseCase,
                    sessionRepository = sessionRepository,
                )
            advanceUntilIdle()

            assertEquals(listOf(post), sut.uiStateFlow.value.explorePosts)
            assertFalse(sut.uiStateFlow.value.isLoading)
        }

    @Test
    fun `selecting my applications tab loads player applications`() =
        runTest {
            every { sessionRepository.getWards() } returns emptyList()
            every { sessionRepository.observeSession() } returns flowOf(ArenaSessionState())
            coEvery { getMercenaryPostsUseCase(any()) } returns
                Result.Success(MercenaryPage(emptyList(), 1, 20, 1, 0))
            coEvery { getMyMercenaryApplicationsUseCase() } returns Result.Success(emptyList())

            sut =
                MercenaryListViewModel(
                    applyMercenaryUseCase = applyMercenaryUseCase,
                    getMercenaryPostsUseCase = getMercenaryPostsUseCase,
                    getMyMercenaryApplicationsUseCase = getMyMercenaryApplicationsUseCase,
                    getMyTeamMercenaryPostsUseCase = getMyTeamMercenaryPostsUseCase,
                    sessionRepository = sessionRepository,
                )
            advanceUntilIdle()

            sut.onEvent(MercenaryListEvent.SelectTab(MercenaryListTab.MY_APPLICATIONS))
            advanceUntilIdle()

            assertEquals(MercenaryListTab.MY_APPLICATIONS, sut.uiStateFlow.value.selectedTab)
            assertFalse(sut.uiStateFlow.value.isLoading)
        }

    private fun sampleSummary(id: String) =
        MercenaryPostSummary(
            id = id,
            teamId = "team-alpha",
            teamName = "FC Sao Mai",
            teamAvatarUrl = null,
            captainName = null,
            teamReputationScore = 92,
            position = PlayerPosition.GOALKEEPER,
            neededPositions = listOf(PlayerPosition.GOALKEEPER),
            slotsNeeded = 1,
            slotsFilled = 0,
            pendingApplicationCount = 0,
            status = MercenaryPostStatus.OPEN,
            format = PitchFormat.SEVEN_A_SIDE,
            skillLevel = SkillLevel.INTERMEDIATE,
            wardId = "ward-ak",
            areaLabel = "Phường An Khánh, TP. Thủ Đức",
            distanceKm = 1.5,
            matchStartAt = "2026-11-21T19:00:00",
            matchEndAt = "2026-11-21T20:30:00",
            allowanceVnd = 100_000,
            notes = "Cần 1 thủ môn",
            myApplicationStatus = null,
            myApplicationId = null,
            createdAt = "2026-11-18T10:00:00",
        )
}
