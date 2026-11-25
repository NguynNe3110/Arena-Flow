package com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarydetail

import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.model.SkillLevel
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostDetail
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostStatus
import com.igorwojda.showcase.feature.mercenary.domain.usecase.AcceptMercenaryApplicationUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.ApplyMercenaryUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.CloseMercenaryPostUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetMercenaryPostDetailUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetPostApplicationsUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.RejectMercenaryApplicationUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.WithdrawMercenaryApplicationUseCase
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class MercenaryDetailViewModelTest {
    private val acceptMercenaryApplicationUseCase: AcceptMercenaryApplicationUseCase = mockk()
    private val applyMercenaryUseCase: ApplyMercenaryUseCase = mockk()
    private val closeMercenaryPostUseCase: CloseMercenaryPostUseCase = mockk()
    private val getMercenaryPostDetailUseCase: GetMercenaryPostDetailUseCase = mockk()
    private val getPostApplicationsUseCase: GetPostApplicationsUseCase = mockk()
    private val rejectMercenaryApplicationUseCase: RejectMercenaryApplicationUseCase = mockk()
    private val withdrawMercenaryApplicationUseCase: WithdrawMercenaryApplicationUseCase = mockk()
    private lateinit var sut: MercenaryDetailViewModel

    @Test
    fun `load fetches post detail and captain applications`() =
        runTest {
            val detail =
                MercenaryPostDetail(
                    id = "rec-1",
                    teamId = "team-alpha",
                    teamName = "FC Sao Mai",
                    teamAvatarUrl = null,
                    captainName = "Nguyễn Văn A",
                    teamReputationScore = 92,
                    matchId = null,
                    externalMatchId = null,
                    position = PlayerPosition.GOALKEEPER,
                    neededPositions = listOf(PlayerPosition.GOALKEEPER),
                    slotsNeeded = 1,
                    slotsFilled = 0,
                    pendingApplicationCount = 1,
                    status = MercenaryPostStatus.OPEN,
                    format = PitchFormat.SEVEN_A_SIDE,
                    skillLevel = SkillLevel.INTERMEDIATE,
                    wardId = "ward-ak",
                    areaLabel = "Thủ Đức",
                    distanceKm = 1.5,
                    venueId = "ven-1",
                    venueName = "Sân An Phú",
                    venueAddress = "123 An Phú",
                    matchStartAt = "2026-11-21T19:00:00",
                    matchEndAt = "2026-11-21T20:30:00",
                    allowanceVnd = 100_000,
                    notes = "",
                    viewerIsCaptain = true,
                    myApplication = null,
                    createdAt = "2026-11-18T10:00:00",
                )
            coEvery { getMercenaryPostDetailUseCase("rec-1") } returns Result.Success(detail)
            coEvery { getPostApplicationsUseCase("rec-1") } returns Result.Success(emptyList())

            sut =
                MercenaryDetailViewModel(
                    acceptMercenaryApplicationUseCase = acceptMercenaryApplicationUseCase,
                    applyMercenaryUseCase = applyMercenaryUseCase,
                    closeMercenaryPostUseCase = closeMercenaryPostUseCase,
                    getMercenaryPostDetailUseCase = getMercenaryPostDetailUseCase,
                    getPostApplicationsUseCase = getPostApplicationsUseCase,
                    rejectMercenaryApplicationUseCase = rejectMercenaryApplicationUseCase,
                    withdrawMercenaryApplicationUseCase = withdrawMercenaryApplicationUseCase,
                )
            sut.onEvent(MercenaryDetailEvent.Load("rec-1"))
            advanceUntilIdle()

            assertEquals(detail, sut.uiStateFlow.value.post)
            assertFalse(sut.uiStateFlow.value.isLoading)
        }
}
