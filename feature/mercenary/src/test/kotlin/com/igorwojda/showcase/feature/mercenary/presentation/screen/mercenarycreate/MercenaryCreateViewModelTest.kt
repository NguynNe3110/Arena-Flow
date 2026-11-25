package com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarycreate

import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.model.SelectedVenueResult
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.mercenary.domain.usecase.CreateMercenaryPostUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetMercenaryPostDetailUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.UpdateMercenaryPostUseCase
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class MercenaryCreateViewModelTest {
    private val createMercenaryPostUseCase: CreateMercenaryPostUseCase = mockk()
    private val getMercenaryPostDetailUseCase: GetMercenaryPostDetailUseCase = mockk()
    private val sessionRepository: SessionRepository = mockk()
    private val updateMercenaryPostUseCase: UpdateMercenaryPostUseCase = mockk()
    private lateinit var sut: MercenaryCreateViewModel

    @Test
    fun `session venue selection populates venue fields`() =
        runTest {
            val venue =
                SelectedVenueResult(
                    requestKey = MercenaryCreateViewModel.VENUE_PICKER_KEY,
                    venueId = "ven-1",
                    venueName = "Sân An Phú",
                    venueAddress = "An Khánh, Thủ Đức",
                )
            every { sessionRepository.observeSession() } returns
                flowOf(ArenaSessionState(pendingVenueSelection = venue))
            coEvery { sessionRepository.consumePendingVenueSelection(MercenaryCreateViewModel.VENUE_PICKER_KEY) } returns
                venue

            sut =
                MercenaryCreateViewModel(
                    createMercenaryPostUseCase = createMercenaryPostUseCase,
                    getMercenaryPostDetailUseCase = getMercenaryPostDetailUseCase,
                    sessionRepository = sessionRepository,
                    updateMercenaryPostUseCase = updateMercenaryPostUseCase,
                )
            advanceUntilIdle()

            assertEquals("ven-1", sut.uiStateFlow.value.venueId)
            assertEquals("Sân An Phú", sut.uiStateFlow.value.venueName)

            sut.onEvent(MercenaryCreateEvent.FormatChanged(PitchFormat.FIVE_A_SIDE))
            sut.onEvent(MercenaryCreateEvent.TogglePosition(PlayerPosition.GOALKEEPER))

            assertEquals(PitchFormat.FIVE_A_SIDE, sut.uiStateFlow.value.format)
            assertTrue(PlayerPosition.GOALKEEPER in sut.uiStateFlow.value.neededPositions)
        }
}
