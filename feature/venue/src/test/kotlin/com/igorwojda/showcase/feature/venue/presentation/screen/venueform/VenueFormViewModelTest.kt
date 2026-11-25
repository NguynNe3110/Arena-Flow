package com.igorwojda.showcase.feature.venue.presentation.screen.venueform

import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.SessionUserSummary
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.venue.domain.model.VenueDetail
import com.igorwojda.showcase.feature.venue.domain.model.VenueStatus
import com.igorwojda.showcase.feature.venue.domain.model.VenueWardOption
import com.igorwojda.showcase.feature.venue.domain.usecase.CreateVenueUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.GetVenueDetailUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.GetVenueWardsUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.SelectVenueForCallerUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.UpdateVenueUseCase
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
class VenueFormViewModelTest {
    private val createVenueUseCase: CreateVenueUseCase = mockk()
    private val getSessionStateUseCase: GetSessionStateUseCase = mockk()
    private val getVenueDetailUseCase: GetVenueDetailUseCase = mockk()
    private val getVenueWardsUseCase: GetVenueWardsUseCase = mockk()
    private val selectVenueForCallerUseCase: SelectVenueForCallerUseCase = mockk()
    private val updateVenueUseCase: UpdateVenueUseCase = mockk()
    private lateinit var sut: VenueFormViewModel

    @Test
    fun `when non-creator non-admin edits venue then sets screenError`() =
        runTest {
            val user = sessionUser(id = "user-other", role = AccountRole.USER)
            val venue = venueDetail(createdById = "user-creator")
            every { getSessionStateUseCase() } returns flowOf(ArenaSessionState(currentUser = user))
            coEvery { getVenueWardsUseCase() } returns Result.Success(emptyList())
            coEvery { getVenueDetailUseCase("ven-1") } returns Result.Success(venue)

            sut =
                VenueFormViewModel(
                    createVenueUseCase,
                    getSessionStateUseCase,
                    getVenueDetailUseCase,
                    getVenueWardsUseCase,
                    selectVenueForCallerUseCase,
                    updateVenueUseCase,
                )
            sut.loadForm("ven-1")
            advanceUntilIdle()

            sut.uiStateFlow.value.screenError shouldBeEqualTo
                "Chỉ người tạo sân hoặc Admin mới được phép chỉnh sửa thông tin sân."
        }

    @Test
    fun `when creating venue with returnKey then selects created venue and marks selectionReturned`() =
        runTest {
            val user = sessionUser(id = "user-1", role = AccountRole.USER)
            val created = venueDetail(createdById = "user-1")
            every { getSessionStateUseCase() } returns flowOf(ArenaSessionState(currentUser = user))
            coEvery { getVenueWardsUseCase() } returns
                Result.Success(listOf(VenueWardOption("ward-ak", "Phường An Khánh", "prov-hcm", "TP. Hồ Chí Minh")))
            coEvery { createVenueUseCase(any()) } returns Result.Success(created)
            coEvery { selectVenueForCallerUseCase(any()) } returns Unit

            sut =
                VenueFormViewModel(
                    createVenueUseCase,
                    getSessionStateUseCase,
                    getVenueDetailUseCase,
                    getVenueWardsUseCase,
                    selectVenueForCallerUseCase,
                    updateVenueUseCase,
                )
            sut.loadForm(null)
            advanceUntilIdle()

            sut.onNameChange("Sân Hoa Lư")
            sut.onAddressChange("2 Đinh Tiên Hoàng")
            sut.submit(returnKey = "match-create")
            advanceUntilIdle()

            sut.uiStateFlow.value.savedVenueId shouldBeEqualTo "ven-new"
            sut.uiStateFlow.value.isSelectionReturned shouldBeEqualTo true
            coVerify(exactly = 1) { selectVenueForCallerUseCase(any()) }
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

    private fun venueDetail(createdById: String) =
        VenueDetail(
            venueId = "ven-new",
            venueName = "Sân Hoa Lư",
            address = "2 Đinh Tiên Hoàng",
            latitude = 10.79,
            longitude = 106.74,
            ward = null,
            venueType = PitchFormat.SEVEN_A_SIDE,
            phone = null,
            website = null,
            status = VenueStatus.ACTIVE,
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
