package com.igorwojda.showcase.feature.pairing.presentation.screen.pairingroom

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.pairing.PairingTestFixtures
import com.igorwojda.showcase.feature.pairing.domain.model.PairingStatus
import com.igorwojda.showcase.feature.pairing.domain.usecase.AddPairingOptionUseCase
import com.igorwojda.showcase.feature.pairing.domain.usecase.CancelPairingUseCase
import com.igorwojda.showcase.feature.pairing.domain.usecase.ConfirmPairingUseCase
import com.igorwojda.showcase.feature.pairing.domain.usecase.DisputePairingUseCase
import com.igorwojda.showcase.feature.pairing.domain.usecase.GetPairingRoomUseCase
import com.igorwojda.showcase.feature.pairing.domain.usecase.SelectPairingOptionUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PairingRoomViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val addPairingOptionUseCase: AddPairingOptionUseCase = mockk()
    private val cancelPairingUseCase: CancelPairingUseCase = mockk()
    private val confirmPairingUseCase: ConfirmPairingUseCase = mockk()
    private val disputePairingUseCase: DisputePairingUseCase = mockk()
    private val getPairingRoomUseCase: GetPairingRoomUseCase = mockk()
    private val selectPairingOptionUseCase: SelectPairingOptionUseCase = mockk()
    private lateinit var sut: PairingRoomViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sut =
            PairingRoomViewModel(
                addPairingOptionUseCase = addPairingOptionUseCase,
                cancelPairingUseCase = cancelPairingUseCase,
                confirmPairingUseCase = confirmPairingUseCase,
                disputePairingUseCase = disputePairingUseCase,
                getPairingRoomUseCase = getPairingRoomUseCase,
                selectPairingOptionUseCase = selectPairingOptionUseCase,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when Load succeeds then updates uiState with pairing room`() =
        runTest {
            val room = PairingTestFixtures.sampleRoom()
            coEvery { getPairingRoomUseCase("pairing-beta-alpha") } returns Result.Success(room)

            sut.onEvent(PairingRoomEvent.Load("pairing-beta-alpha"))

            sut.uiStateFlow.value.isLoading shouldBeEqualTo false
            sut.uiStateFlow.value.pairingRoom shouldBeEqualTo room
            sut.uiStateFlow.value.error shouldBeEqualTo null
        }

    @Test
    fun `when RequestConfirm and other captain also selected then shows double confirm dialog`() =
        runTest {
            val room = PairingTestFixtures.sampleRoom(otherSelected = true)
            coEvery { getPairingRoomUseCase("pairing-beta-alpha") } returns Result.Success(room)
            sut.onEvent(PairingRoomEvent.Load("pairing-beta-alpha"))

            sut.onEvent(PairingRoomEvent.RequestConfirm)

            sut.uiStateFlow.value.showDoubleConfirmDialog shouldBeEqualTo true
        }

    @Test
    fun `when ConfirmPackage succeeds then updates room status to CONFIRMED`() =
        runTest {
            val initial = PairingTestFixtures.sampleRoom()
            val confirmed =
                PairingTestFixtures.sampleRoom(
                    status = PairingStatus.CONFIRMED,
                    hostConfirmed = true,
                    guestConfirmed = true,
                )
            coEvery { getPairingRoomUseCase("pairing-beta-alpha") } returns Result.Success(initial)
            coEvery { confirmPairingUseCase("pairing-beta-alpha", 1, "opt-1") } returns Result.Success(confirmed)

            sut.onEvent(PairingRoomEvent.Load("pairing-beta-alpha"))
            sut.onEvent(PairingRoomEvent.ConfirmPackage)

            sut.uiStateFlow.value.pairingRoom
                ?.status shouldBeEqualTo PairingStatus.CONFIRMED
            sut.uiStateFlow.value.showDoubleConfirmDialog shouldBeEqualTo false
        }

    @Test
    fun `when Dispute fails then sets inlineError without clearing room`() =
        runTest {
            val initial = PairingTestFixtures.sampleRoom()
            val error = AppError.DomainError(400, "VALIDATION_ERROR", "Vui lòng nhập lý do tranh chấp.")
            coEvery { getPairingRoomUseCase("pairing-beta-alpha") } returns Result.Success(initial)
            coEvery { disputePairingUseCase("pairing-beta-alpha", "") } returns Result.Failure(error)

            sut.onEvent(PairingRoomEvent.Load("pairing-beta-alpha"))
            sut.onEvent(PairingRoomEvent.Dispute(""))

            sut.uiStateFlow.value.pairingRoom shouldBeEqualTo initial
            sut.uiStateFlow.value.inlineError shouldBeEqualTo error
        }
}
