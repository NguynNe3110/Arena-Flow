@file:Suppress("MagicNumber", "ReturnCount", "TooManyFunctions")

package com.igorwojda.showcase.feature.profile.presentation.screen.profileedit

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.profile.domain.model.ProfileGender
import com.igorwojda.showcase.feature.profile.domain.model.Province
import com.igorwojda.showcase.feature.profile.domain.model.UpdateProfileParams
import com.igorwojda.showcase.feature.profile.domain.model.UserProfile
import com.igorwojda.showcase.feature.profile.domain.model.Ward
import com.igorwojda.showcase.feature.profile.domain.usecase.GetMyProfileUseCase
import com.igorwojda.showcase.feature.profile.domain.usecase.GetProvincesUseCase
import com.igorwojda.showcase.feature.profile.domain.usecase.GetWardsByProvinceUseCase
import com.igorwojda.showcase.feature.profile.domain.usecase.UpdateMyProfileUseCase
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period

data class ProfileEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isFirstTime: Boolean = false,
    val displayName: String = "",
    val bio: String = "",
    val preferredPosition: PlayerPosition = PlayerPosition.MIDFIELDER,
    val dateOfBirth: String = "1998-05-15",
    val gender: ProfileGender = ProfileGender.MALE,
    val provinces: List<Province> = emptyList(),
    val selectedProvinceId: String? = null,
    val wards: List<Ward> = emptyList(),
    val isLoadingWards: Boolean = false,
    val selectedWardId: String? = null,
    val displayNameError: String? = null,
    val dateOfBirthError: String? = null,
    val submitError: String? = null,
) : BaseState {
    val selectedProvinceName: String
        get() = provinces.firstOrNull { it.provinceId == selectedProvinceId }?.provinceName ?: "Chọn tỉnh/thành"

    val selectedWardName: String
        get() = wards.firstOrNull { it.wardId == selectedWardId }?.wardName ?: "Chọn phường/xã"
}

sealed interface ProfileEditAction : BaseAction<ProfileEditUiState> {
    data class SetFirstTime(
        val isFirstTime: Boolean,
    ) : ProfileEditAction {
        override fun reduce(state: ProfileEditUiState): ProfileEditUiState = state.copy(isFirstTime = isFirstTime)
    }

    data object Loading : ProfileEditAction {
        override fun reduce(state: ProfileEditUiState): ProfileEditUiState = state.copy(isLoading = true, submitError = null)
    }

    data class InitialLoaded(
        val profile: UserProfile?,
        val provinces: List<Province>,
        val wards: List<Ward>,
    ) : ProfileEditAction {
        override fun reduce(state: ProfileEditUiState): ProfileEditUiState =
            state.copy(
                isLoading = false,
                displayName = profile?.displayName.orEmpty(),
                bio = profile?.bio.orEmpty(),
                preferredPosition = profile?.preferredPosition ?: PlayerPosition.MIDFIELDER,
                dateOfBirth = profile?.dateOfBirth ?: state.dateOfBirth,
                gender = profile?.gender ?: ProfileGender.MALE,
                provinces = provinces,
                selectedProvinceId = profile?.ward?.provinceId,
                wards = wards,
                selectedWardId = profile?.ward?.wardId,
            )
    }

    data class DisplayNameChanged(
        val value: String,
    ) : ProfileEditAction {
        override fun reduce(state: ProfileEditUiState): ProfileEditUiState =
            state.copy(displayName = value, displayNameError = null, submitError = null)
    }

    data class BioChanged(
        val value: String,
    ) : ProfileEditAction {
        override fun reduce(state: ProfileEditUiState): ProfileEditUiState =
            state.copy(bio = value.take(MAX_BIO_LENGTH), submitError = null)
    }

    data class PositionSelected(
        val position: PlayerPosition,
    ) : ProfileEditAction {
        override fun reduce(state: ProfileEditUiState): ProfileEditUiState = state.copy(preferredPosition = position, submitError = null)
    }

    data class DateOfBirthChanged(
        val value: String,
    ) : ProfileEditAction {
        override fun reduce(state: ProfileEditUiState): ProfileEditUiState =
            state.copy(dateOfBirth = value, dateOfBirthError = null, submitError = null)
    }

    data class GenderSelected(
        val gender: ProfileGender,
    ) : ProfileEditAction {
        override fun reduce(state: ProfileEditUiState): ProfileEditUiState = state.copy(gender = gender, submitError = null)
    }

    data class ProvinceSelected(
        val provinceId: String,
    ) : ProfileEditAction {
        override fun reduce(state: ProfileEditUiState): ProfileEditUiState =
            state.copy(
                selectedProvinceId = provinceId,
                selectedWardId = null,
                wards = emptyList(),
                isLoadingWards = true,
                submitError = null,
            )
    }

    data class WardsLoaded(
        val wards: List<Ward>,
    ) : ProfileEditAction {
        override fun reduce(state: ProfileEditUiState): ProfileEditUiState = state.copy(wards = wards, isLoadingWards = false)
    }

    data class WardSelected(
        val wardId: String,
    ) : ProfileEditAction {
        override fun reduce(state: ProfileEditUiState): ProfileEditUiState = state.copy(selectedWardId = wardId, submitError = null)
    }

    data class ValidationFailed(
        val displayNameError: String?,
        val dateOfBirthError: String?,
    ) : ProfileEditAction {
        override fun reduce(state: ProfileEditUiState): ProfileEditUiState =
            state.copy(
                displayNameError = displayNameError,
                dateOfBirthError = dateOfBirthError,
            )
    }

    data object Saving : ProfileEditAction {
        override fun reduce(state: ProfileEditUiState): ProfileEditUiState = state.copy(isSaving = true, submitError = null)
    }

    data object Saved : ProfileEditAction {
        override fun reduce(state: ProfileEditUiState): ProfileEditUiState = state.copy(isSaving = false, isSaved = true)
    }

    data class SaveFailed(
        val message: String,
        val displayNameError: String? = null,
        val dateOfBirthError: String? = null,
    ) : ProfileEditAction {
        override fun reduce(state: ProfileEditUiState): ProfileEditUiState =
            state.copy(
                isSaving = false,
                submitError = message,
                displayNameError = displayNameError ?: state.displayNameError,
                dateOfBirthError = dateOfBirthError ?: state.dateOfBirthError,
            )
    }

    private companion object {
        const val MAX_BIO_LENGTH = 1000
    }
}

class ProfileEditViewModel(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getProvincesUseCase: GetProvincesUseCase,
    private val getWardsByProvinceUseCase: GetWardsByProvinceUseCase,
    private val updateMyProfileUseCase: UpdateMyProfileUseCase,
) : BaseViewModel<ProfileEditUiState, ProfileEditAction>(ProfileEditUiState()) {
    init {
        loadInitialData()
    }

    fun setFirstTime(isFirstTime: Boolean) {
        sendAction(ProfileEditAction.SetFirstTime(isFirstTime))
    }

    fun loadInitialData() {
        viewModelScope.launch {
            sendAction(ProfileEditAction.Loading)
            val profileResult = getMyProfileUseCase()
            val provincesResult = getProvincesUseCase()

            val profile = (profileResult as? Result.Success)?.value
            val provinces = (provincesResult as? Result.Success)?.value.orEmpty()
            val initialProvinceId = profile?.ward?.provinceId
            val wards =
                if (!initialProvinceId.isNullOrBlank()) {
                    (getWardsByProvinceUseCase(initialProvinceId) as? Result.Success)?.value.orEmpty()
                } else {
                    emptyList()
                }

            sendAction(
                ProfileEditAction.InitialLoaded(
                    profile = profile,
                    provinces = provinces,
                    wards = wards,
                ),
            )
        }
    }

    fun onDisplayNameChanged(value: String) = sendAction(ProfileEditAction.DisplayNameChanged(value))

    fun onBioChanged(value: String) = sendAction(ProfileEditAction.BioChanged(value))

    fun onPositionSelected(position: PlayerPosition) = sendAction(ProfileEditAction.PositionSelected(position))

    fun onDateOfBirthChanged(value: String) = sendAction(ProfileEditAction.DateOfBirthChanged(value))

    fun onGenderSelected(gender: ProfileGender) = sendAction(ProfileEditAction.GenderSelected(gender))

    fun onProvinceSelected(provinceId: String) {
        sendAction(ProfileEditAction.ProvinceSelected(provinceId))
        viewModelScope.launch {
            val wards =
                when (val result = getWardsByProvinceUseCase(provinceId)) {
                    is Result.Success -> result.value
                    is Result.Failure -> emptyList()
                }
            sendAction(ProfileEditAction.WardsLoaded(wards))
        }
    }

    fun onWardSelected(wardId: String) = sendAction(ProfileEditAction.WardSelected(wardId))

    fun saveProfile(today: LocalDate = LocalDate.now()) {
        val state = uiStateFlow.value
        val trimmedName = state.displayName.trim()
        val nameError =
            when {
                trimmedName.isBlank() -> "Tên hiển thị không được để trống"
                trimmedName.length > MAX_DISPLAY_NAME_LENGTH -> "Tên hiển thị tối đa 100 ký tự"
                else -> null
            }
        val dobTrimmed = state.dateOfBirth.trim()
        val dobError = validateDateOfBirth(dobTrimmed, today)

        if (nameError != null || dobError != null) {
            sendAction(ProfileEditAction.ValidationFailed(nameError, dobError))
            return
        }

        viewModelScope.launch {
            sendAction(ProfileEditAction.Saving)
            val result =
                updateMyProfileUseCase(
                    UpdateProfileParams(
                        displayName = trimmedName,
                        bio = state.bio.trim().ifEmpty { null },
                        preferredPosition = state.preferredPosition,
                        dateOfBirth = dobTrimmed.ifBlank { null },
                        gender = state.gender,
                        wardId = state.selectedWardId,
                    ),
                )
            when (result) {
                is Result.Success -> {
                    sendAction(ProfileEditAction.Saved)
                }

                is Result.Failure -> {
                    val domainError = result.error as? AppError.DomainError
                    sendAction(
                        ProfileEditAction.SaveFailed(
                            message = result.error.userMessage("Không thể lưu hồ sơ, vui lòng thử lại."),
                            displayNameError = domainError?.fieldErrors?.get("displayName"),
                            dateOfBirthError = domainError?.fieldErrors?.get("dateOfBirth"),
                        ),
                    )
                }
            }
        }
    }

    private fun validateDateOfBirth(
        dob: String,
        today: LocalDate,
    ): String? {
        if (dob.isBlank()) return null
        val parsed =
            runCatching { LocalDate.parse(dob) }.getOrNull()
                ?: return "Ngày sinh phải đúng định dạng YYYY-MM-DD"
        if (!parsed.isBefore(today)) {
            return "Ngày sinh phải ở trong quá khứ"
        }
        if (Period.between(parsed, today).years < MIN_AGE_YEARS) {
            return "Bạn phải đủ 13 tuổi"
        }
        return null
    }

    private companion object {
        const val MAX_DISPLAY_NAME_LENGTH = 100
        const val MIN_AGE_YEARS = 13
    }
}
