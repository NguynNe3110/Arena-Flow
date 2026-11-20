package com.uzuu.match.presentation.screen.pairingroom

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import com.uzuu.match.domain.model.PairingRoom
import com.uzuu.match.domain.model.PairingTeam
import com.uzuu.match.domain.usecase.ConfirmPackageUseCase
import com.uzuu.match.domain.usecase.GetPairingRoomUseCase
import com.uzuu.match.domain.usecase.SelectPairingOptionUseCase
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
class PairingRoomViewModelTest {
    private val getPairingRoomUseCase: GetPairingRoomUseCase = mockk()
    private val confirmPackageUseCase: ConfirmPackageUseCase = mockk()
    private val selectPairingOptionUseCase: SelectPairingOptionUseCase = mockk()

    @Test
    fun `load loads pairing room successfully`() =
        runTest {
            val room = sampleRoom()
            coEvery { getPairingRoomUseCase("p1") } returns Result.Success(room)

            val viewModel = PairingRoomViewModel(getPairingRoomUseCase, confirmPackageUseCase, selectPairingOptionUseCase)
            viewModel.load("p1")
            advanceUntilIdle()

            assertEquals(room, viewModel.uiState.value.pairingRoom)
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `confirmPackage confirms package successfully`() =
        runTest {
            val room = sampleRoom(guestConfirmed = false)
            val confirmed = room.copy(guestConfirmed = true)
            coEvery { getPairingRoomUseCase("p1") } returns Result.Success(room)
            coEvery { confirmPackageUseCase("p1") } returns Result.Success(confirmed)

            val viewModel = PairingRoomViewModel(getPairingRoomUseCase, confirmPackageUseCase, selectPairingOptionUseCase)
            viewModel.load("p1")
            advanceUntilIdle()

            viewModel.confirmPackage()
            advanceUntilIdle()

            assertTrue(
                viewModel.uiState.value.pairingRoom
                    ?.guestConfirmed == true,
            )
            assertFalse(viewModel.uiState.value.isConfirming)
        }

    @Test
    fun `selectVenue updates room and resets confirmations`() =
        runTest {
            val room = sampleRoom(selectedVenueId = "v1")
            val updated = room.copy(selectedVenueId = "v2", hostConfirmed = false, guestConfirmed = false)
            coEvery { getPairingRoomUseCase("p1") } returns Result.Success(room)
            coEvery { selectPairingOptionUseCase("p1", "v2", null) } returns Result.Success(updated)

            val viewModel = PairingRoomViewModel(getPairingRoomUseCase, confirmPackageUseCase, selectPairingOptionUseCase)
            viewModel.load("p1")
            advanceUntilIdle()

            viewModel.selectVenue("v2")
            advanceUntilIdle()

            assertEquals(
                "v2",
                viewModel.uiState.value.pairingRoom
                    ?.selectedVenueId,
            )
            assertFalse(
                viewModel.uiState.value.pairingRoom
                    ?.guestConfirmed == true,
            )
        }

    private fun sampleRoom(
        selectedVenueId: String = "v1",
        guestConfirmed: Boolean = false,
    ) = PairingRoom(
        pairingId = "p1",
        matchId = "m1",
        hostTeam = PairingTeam("t1", "Host FC", "HFC", 90),
        guestTeam = PairingTeam("t2", "Guest FC", "GFC", 92),
        venueOptions = emptyList(),
        selectedVenueId = selectedVenueId,
        timeOptions = emptyList(),
        selectedTimeSlot = "19:00",
        guestConfirmed = guestConfirmed,
    )
}
