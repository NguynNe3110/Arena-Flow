package com.igorwojda.showcase.feature.venue.presentation.screen.venuedetail

import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.SessionUserSummary
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.venue.domain.model.VenueDetail
import com.igorwojda.showcase.feature.venue.domain.model.VenueReport
import com.igorwojda.showcase.feature.venue.domain.model.VenueReportReason
import com.igorwojda.showcase.feature.venue.domain.model.VenueStatus
import com.igorwojda.showcase.feature.venue.domain.usecase.GetVenueDetailUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.ReportVenueUseCase
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
class VenueDetailViewModelTest {
    private val getSessionStateUseCase: GetSessionStateUseCase = mockk()
    private val getVenueDetailUseCase: GetVenueDetailUseCase = mockk()
    private val reportVenueUseCase: ReportVenueUseCase = mockk()
    private val selectVenueForCallerUseCase: SelectVenueForCallerUseCase = mockk()
    private lateinit var sut: VenueDetailViewModel

    @Test
    fun `when creator loads active venue then canEdit and canReport are true`() =
        runTest {
            val user = sessionUser(id = "user-1", role = AccountRole.USER)
            val venue = venueDetail(createdById = "user-1", status = VenueStatus.ACTIVE)
            every { getSessionStateUseCase() } returns flowOf(ArenaSessionState(currentUser = user))
            coEvery { getVenueDetailUseCase("ven-1") } returns Result.Success(venue)

            sut =
                VenueDetailViewModel(
                    getSessionStateUseCase,
                    getVenueDetailUseCase,
                    reportVenueUseCase,
                    selectVenueForCallerUseCase,
                )
            sut.loadVenue("ven-1")
            advanceUntilIdle()

            sut.uiStateFlow.value.canEdit shouldBeEqualTo true
            sut.uiStateFlow.value.canReport shouldBeEqualTo true
        }

    @Test
    fun `when submitting empty report detail then sets validation error`() =
        runTest {
            val user = sessionUser(id = "user-2", role = AccountRole.USER)
            val venue = venueDetail(createdById = "user-1", status = VenueStatus.ACTIVE)
            every { getSessionStateUseCase() } returns flowOf(ArenaSessionState(currentUser = user))
            coEvery { getVenueDetailUseCase("ven-1") } returns Result.Success(venue)

            sut =
                VenueDetailViewModel(
                    getSessionStateUseCase,
                    getVenueDetailUseCase,
                    reportVenueUseCase,
                    selectVenueForCallerUseCase,
                )
            sut.loadVenue("ven-1")
            advanceUntilIdle()

            sut.openReportSheet()
            sut.submitReport()

            sut.uiStateFlow.value.reportDetailError shouldBeEqualTo "Vui lòng nhập mô tả sai sót."
            coVerify(exactly = 0) { reportVenueUseCase(any(), any(), any()) }
        }

    @Test
    fun `when submitting valid report then closes sheet and shows snackbar`() =
        runTest {
            val user = sessionUser(id = "user-2", role = AccountRole.USER)
            val venue = venueDetail(createdById = "user-1", status = VenueStatus.ACTIVE)
            every { getSessionStateUseCase() } returns flowOf(ArenaSessionState(currentUser = user))
            coEvery { getVenueDetailUseCase("ven-1") } returns Result.Success(venue)
            coEvery { reportVenueUseCase("ven-1", VenueReportReason.CLOSED, "Đã giải tỏa") } returns
                Result.Success(
                    VenueReport("rep-1", "ven-1", VenueReportReason.CLOSED, "Đã giải tỏa", "PENDING", "2026-11-18T10:00:00"),
                )

            sut =
                VenueDetailViewModel(
                    getSessionStateUseCase,
                    getVenueDetailUseCase,
                    reportVenueUseCase,
                    selectVenueForCallerUseCase,
                )
            sut.loadVenue("ven-1")
            advanceUntilIdle()

            sut.openReportSheet()
            sut.onReportDetailChange("Đã giải tỏa")
            sut.submitReport()
            advanceUntilIdle()

            sut.uiStateFlow.value.isReportSheetOpen shouldBeEqualTo false
            sut.uiStateFlow.value.snackbarMessage shouldBeEqualTo "Đã gửi báo cáo thông tin sân cho Admin."
        }

    private fun sessionUser(
        id: String,
        role: AccountRole,
    ) = SessionUserSummary(
        id = id,
        phone = "0900000001",
        displayName = "Test User",
        preferredPosition = null,
        wardId = "ward-ak",
        role = role,
        isNewUser = false,
        reputationScore = 100,
        matchCount = 5,
    )

    private fun venueDetail(
        createdById: String,
        status: VenueStatus,
    ) = VenueDetail(
        venueId = "ven-1",
        venueName = "Sân An Phú",
        address = "Số 8 Đường số 2",
        latitude = 10.79,
        longitude = 106.74,
        ward = null,
        venueType = PitchFormat.SEVEN_A_SIDE,
        phone = null,
        website = null,
        status = status,
        createdByType = AccountRole.USER,
        createdById = createdById,
        createdByName = "User",
        lastSelectedAt = null,
        adminVerifiedInactiveAt = null,
        createdAt = "2026-11-18T10:00:00",
        updatedAt = "2026-11-18T10:00:00",
        images = emptyList(),
    )
}
