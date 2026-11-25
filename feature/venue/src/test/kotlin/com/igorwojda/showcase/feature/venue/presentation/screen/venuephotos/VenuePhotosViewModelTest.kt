@file:Suppress("LongMethod")

package com.igorwojda.showcase.feature.venue.presentation.screen.venuephotos

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.SessionUserSummary
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.venue.domain.model.VenueDetail
import com.igorwojda.showcase.feature.venue.domain.model.VenueImage
import com.igorwojda.showcase.feature.venue.domain.model.VenueStatus
import com.igorwojda.showcase.feature.venue.domain.usecase.DeleteVenueImageUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.GetVenueDetailUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.SetPrimaryVenueImageUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.UploadVenueImageUseCase
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
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
class VenuePhotosViewModelTest {
    private val deleteVenueImageUseCase: DeleteVenueImageUseCase = mockk()
    private val getSessionStateUseCase: GetSessionStateUseCase = mockk()
    private val getVenueDetailUseCase: GetVenueDetailUseCase = mockk()
    private val setPrimaryVenueImageUseCase: SetPrimaryVenueImageUseCase = mockk()
    private val uploadVenueImageUseCase: UploadVenueImageUseCase = mockk()
    private lateinit var sut: VenuePhotosViewModel

    @Test
    fun `when upload fails then stores failed label and retry succeeds`() =
        runTest {
            val user =
                SessionUserSummary(
                    id = "user-1",
                    phone = "0900000001",
                    displayName = "Test",
                    preferredPosition = null,
                    wardId = "ward-ak",
                    role = AccountRole.USER,
                    isNewUser = false,
                    reputationScore = 100,
                    matchCount = 5,
                )
            val venue =
                VenueDetail(
                    venueId = "ven-1",
                    venueName = "Sân An Phú",
                    address = "Số 8",
                    latitude = 10.79,
                    longitude = 106.74,
                    ward = null,
                    venueType = PitchFormat.SEVEN_A_SIDE,
                    phone = null,
                    website = null,
                    status = VenueStatus.ACTIVE,
                    createdByType = AccountRole.USER,
                    createdById = "user-1",
                    createdByName = "Test",
                    lastSelectedAt = null,
                    adminVerifiedInactiveAt = null,
                    createdAt = "2026-11-18T10:00:00",
                    updatedAt = "2026-11-18T10:00:00",
                    images = emptyList(),
                )
            val uploaded =
                VenueImage(
                    imageId = "vp-new",
                    venueId = "ven-1",
                    imageUrl = "local://vp-new",
                    isPrimary = true,
                    displayOrder = 1,
                    uploadedById = "user-1",
                    createdAt = "2026-11-18T10:00:00",
                )

            every { getSessionStateUseCase() } returns flowOf(ArenaSessionState(currentUser = user))
            coEvery { getVenueDetailUseCase("ven-1") } returns Result.Success(venue)
            coEvery { uploadVenueImageUseCase("ven-1", "Mặt sân #1", true) } returnsMany
                listOf(Result.Failure(AppError.Network), Result.Success(uploaded))

            sut =
                VenuePhotosViewModel(
                    deleteVenueImageUseCase,
                    getSessionStateUseCase,
                    getVenueDetailUseCase,
                    setPrimaryVenueImageUseCase,
                    uploadVenueImageUseCase,
                )
            sut.loadPhotos("ven-1")
            advanceUntilIdle()

            sut.uploadPhoto("Mặt sân #1")
            advanceUntilIdle()

            sut.uiStateFlow.value.failedUploadLabel shouldBeEqualTo "Mặt sân #1"
            sut.uiStateFlow.value.images.size shouldBeEqualTo 0

            sut.retryFailedUpload()
            advanceUntilIdle()

            sut.uiStateFlow.value.failedUploadLabel shouldBeEqualTo null
            sut.uiStateFlow.value.images shouldBeEqualTo listOf(uploaded)
        }

    @Test
    fun `when deleting photo then updates images list`() =
        runTest {
            val img = VenueImage("vp-1", "ven-1", "local://vp-1", true, 1, "user-1", "2026-11-18T10:00:00")
            val venue =
                VenueDetail(
                    venueId = "ven-1",
                    venueName = "Sân An Phú",
                    address = "Số 8",
                    latitude = 10.79,
                    longitude = 106.74,
                    ward = null,
                    venueType = PitchFormat.SEVEN_A_SIDE,
                    phone = null,
                    website = null,
                    status = VenueStatus.ACTIVE,
                    createdByType = AccountRole.USER,
                    createdById = "user-1",
                    createdByName = "Test",
                    lastSelectedAt = null,
                    adminVerifiedInactiveAt = null,
                    createdAt = "2026-11-18T10:00:00",
                    updatedAt = "2026-11-18T10:00:00",
                    images = listOf(img),
                )
            every { getSessionStateUseCase() } returns flowOf(ArenaSessionState())
            coEvery { getVenueDetailUseCase("ven-1") } returns Result.Success(venue)
            coEvery { deleteVenueImageUseCase("ven-1", "vp-1") } returns Result.Success(emptyList())

            sut =
                VenuePhotosViewModel(
                    deleteVenueImageUseCase,
                    getSessionStateUseCase,
                    getVenueDetailUseCase,
                    setPrimaryVenueImageUseCase,
                    uploadVenueImageUseCase,
                )
            sut.loadPhotos("ven-1")
            advanceUntilIdle()

            sut.deletePhoto("vp-1")
            advanceUntilIdle()

            sut.uiStateFlow.value.images shouldBeEqualTo emptyList()
            sut.uiStateFlow.value.snackbarMessage shouldBeEqualTo "Đã xóa ảnh sân."
        }
}
