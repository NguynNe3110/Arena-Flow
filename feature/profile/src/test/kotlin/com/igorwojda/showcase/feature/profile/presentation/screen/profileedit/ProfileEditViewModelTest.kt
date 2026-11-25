package com.igorwojda.showcase.feature.profile.presentation.screen.profileedit

import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.profile.domain.model.ProfileGender
import com.igorwojda.showcase.feature.profile.domain.model.Province
import com.igorwojda.showcase.feature.profile.domain.model.UserProfile
import com.igorwojda.showcase.feature.profile.domain.model.Ward
import com.igorwojda.showcase.feature.profile.domain.usecase.GetMyProfileUseCase
import com.igorwojda.showcase.feature.profile.domain.usecase.GetProvincesUseCase
import com.igorwojda.showcase.feature.profile.domain.usecase.GetWardsByProvinceUseCase
import com.igorwojda.showcase.feature.profile.domain.usecase.UpdateMyProfileUseCase
import io.mockk.coEvery
import io.mockk.coVerify
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileEditViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val getMyProfileUseCase: GetMyProfileUseCase = mockk()
    private val getProvincesUseCase: GetProvincesUseCase = mockk()
    private val getWardsByProvinceUseCase: GetWardsByProvinceUseCase = mockk()
    private val updateMyProfileUseCase: UpdateMyProfileUseCase = mockk()
    private lateinit var sut: ProfileEditViewModel

    private val sampleProfile =
        UserProfile(
            userId = "user-1",
            role = AccountRole.USER,
            providers = emptyList(),
            phoneNumber = "0987654321",
            displayName = "Nguyễn Văn A",
            avatarUrl = null,
            bio = "Đá sân 7",
            preferredPosition = PlayerPosition.MIDFIELDER,
            dateOfBirth = "1995-05-15",
            gender = ProfileGender.MALE,
            ward = null,
            createdAt = "2026-11-01T17:00:00",
            updatedAt = "2026-11-02T17:00:00",
        )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getMyProfileUseCase() } returns Result.Success(sampleProfile)
        coEvery { getProvincesUseCase() } returns
            Result.Success(listOf(Province("province-hcm", "TP. Hồ Chí Minh")))
        coEvery { getWardsByProvinceUseCase("province-hcm") } returns
            Result.Success(listOf(Ward("ward-ak", "Phường An Khánh")))
        sut =
            ProfileEditViewModel(
                getMyProfileUseCase = getMyProfileUseCase,
                getProvincesUseCase = getProvincesUseCase,
                getWardsByProvinceUseCase = getWardsByProvinceUseCase,
                updateMyProfileUseCase = updateMyProfileUseCase,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when blank displayName saved then sets validation error and skips api`() =
        runTest {
            sut.onDisplayNameChanged("   ")

            sut.saveProfile(today = LocalDate.of(2026, 11, 18))

            sut.uiStateFlow.value.displayNameError shouldBeEqualTo "Tên hiển thị không được để trống"
            sut.uiStateFlow.value.isSaved shouldBeEqualTo false
            coVerify(exactly = 0) { updateMyProfileUseCase(any()) }
        }

    @Test
    fun `when age under 13 saved then sets dateOfBirth error and skips api`() =
        runTest {
            sut.onDateOfBirthChanged("2015-01-01")

            sut.saveProfile(today = LocalDate.of(2026, 11, 18))

            sut.uiStateFlow.value.dateOfBirthError shouldBeEqualTo "Bạn phải đủ 13 tuổi"
            coVerify(exactly = 0) { updateMyProfileUseCase(any()) }
        }

    @Test
    fun `when province selected then loads wards for that province`() =
        runTest {
            sut.onProvinceSelected("province-hcm")

            sut.uiStateFlow.value.selectedProvinceId shouldBeEqualTo "province-hcm"
            sut.uiStateFlow.value.wards shouldBeEqualTo listOf(Ward("ward-ak", "Phường An Khánh"))
        }

    @Test
    fun `when valid form saved then calls update usecase and marks saved`() =
        runTest {
            coEvery { updateMyProfileUseCase(any()) } returns Result.Success(sampleProfile)
            sut.onPositionSelected(PlayerPosition.GOALKEEPER)
            sut.onProvinceSelected("province-hcm")
            sut.onWardSelected("ward-ak")

            sut.saveProfile(today = LocalDate.of(2026, 11, 18))

            sut.uiStateFlow.value.isSaved shouldBeEqualTo true
            coVerify(exactly = 1) { updateMyProfileUseCase(any()) }
        }
}
