package com.igorwojda.showcase.feature.venue.presentation.screen.venuepicker

import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.SelectedVenueResult
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.venue.domain.model.VenueStatus
import com.igorwojda.showcase.feature.venue.domain.model.VenueSummary
import com.igorwojda.showcase.feature.venue.domain.model.VenueWardOption
import com.igorwojda.showcase.feature.venue.domain.usecase.GetVenueWardsUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.GetVenuesUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.SelectVenueForCallerUseCase
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class VenuePickerViewModelTest {
    private val getSessionStateUseCase: GetSessionStateUseCase = mockk()
    private val getVenueWardsUseCase: GetVenueWardsUseCase = mockk()
    private val getVenuesUseCase: GetVenuesUseCase = mockk()
    private val selectVenueForCallerUseCase: SelectVenueForCallerUseCase = mockk()
    private lateinit var sut: VenuePickerViewModel

    @Test
    fun `when initialized then loads venues and wards and applies format filter`() =
        runTest {
            val active7 = venueSummary("ven-1", "Sân An Phú", PitchFormat.SEVEN_A_SIDE, VenueStatus.ACTIVE)
            val active5 = venueSummary("ven-2", "Sân Thảo Điền", PitchFormat.FIVE_A_SIDE, VenueStatus.ACTIVE)
            val inactive7 = venueSummary("ven-3", "Sân Cũ", PitchFormat.SEVEN_A_SIDE, VenueStatus.INACTIVE)

            every { getSessionStateUseCase() } returns flowOf(ArenaSessionState())
            coEvery { getVenueWardsUseCase() } returns
                Result.Success(listOf(VenueWardOption("ward-ak", "Phường An Khánh", "prov-hcm", "TP. Hồ Chí Minh")))
            coEvery { getVenuesUseCase(query = "") } returns Result.Success(listOf(active7, active5, inactive7))

            sut =
                VenuePickerViewModel(
                    getSessionStateUseCase,
                    getVenueWardsUseCase,
                    getVenuesUseCase,
                    selectVenueForCallerUseCase,
                )
            advanceUntilIdle()

            sut.uiStateFlow.value.visibleVenues.size shouldBeEqualTo 3

            sut.applyFilters(PitchFormat.FIVE_A_SIDE, null)
            sut.uiStateFlow.value.visibleVenues shouldBeEqualTo listOf(active5)
        }

    @Test
    fun `when selecting inactive venue then ignores selection`() =
        runTest {
            val inactive = venueSummary("ven-3", "Sân Cũ", PitchFormat.SEVEN_A_SIDE, VenueStatus.INACTIVE)
            every { getSessionStateUseCase() } returns flowOf(ArenaSessionState())
            coEvery { getVenueWardsUseCase() } returns Result.Success(emptyList())
            coEvery { getVenuesUseCase(query = "") } returns Result.Success(listOf(inactive))

            sut =
                VenuePickerViewModel(
                    getSessionStateUseCase,
                    getVenueWardsUseCase,
                    getVenuesUseCase,
                    selectVenueForCallerUseCase,
                )
            advanceUntilIdle()

            sut.selectVenue("match-create", inactive)
            advanceUntilIdle()

            sut.uiStateFlow.value.isSelectionCompleted shouldBeEqualTo false
            coVerify(exactly = 0) { selectVenueForCallerUseCase(any()) }
        }

    @Test
    fun `when selecting active venue then persists selection and marks completed`() =
        runTest {
            val active = venueSummary("ven-1", "Sân An Phú", PitchFormat.SEVEN_A_SIDE, VenueStatus.ACTIVE)
            every { getSessionStateUseCase() } returns flowOf(ArenaSessionState())
            coEvery { getVenueWardsUseCase() } returns Result.Success(emptyList())
            coEvery { getVenuesUseCase(query = "") } returns Result.Success(listOf(active))
            coEvery { selectVenueForCallerUseCase(any()) } returns Unit

            sut =
                VenuePickerViewModel(
                    getSessionStateUseCase,
                    getVenueWardsUseCase,
                    getVenuesUseCase,
                    selectVenueForCallerUseCase,
                )
            advanceUntilIdle()

            sut.selectVenue("match-create", active)
            advanceUntilIdle()

            sut.uiStateFlow.value.isSelectionCompleted shouldBeEqualTo true
            coVerify(exactly = 1) {
                selectVenueForCallerUseCase(
                    SelectedVenueResult(
                        requestKey = "match-create",
                        venueId = "ven-1",
                        venueName = "Sân An Phú",
                        venueAddress = "Địa chỉ ven-1",
                    ),
                )
            }
        }

    private fun venueSummary(
        id: String,
        name: String,
        format: PitchFormat,
        status: VenueStatus,
    ) = VenueSummary(
        venueId = id,
        venueName = name,
        address = "Địa chỉ $id",
        latitude = 10.79,
        longitude = 106.74,
        venueType = format,
        status = status,
        primaryImageUrl = null,
        distanceKm = 1.0,
        lastSelectedAt = null,
    )
}
