package com.uzuu.match.presentation.screen.teamcreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.TeamDraft
import com.uzuu.match.domain.usecase.CreateTeamUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class TeamCreateUiState(
    val teamName: String = "",
    val shortName: String = "",
    val description: String = "",
    val selectedWard: String = "P. An Khánh (TP. Thủ Đức, TP.HCM)",
    val selectedSkills: Set<String> = setOf("Trung bình"),
    val nameError: String? = null,
    val wardError: String? = null,
    val isSubmitting: Boolean = false,
    val isCreatedSuccessfully: Boolean = false,
    val createdTeamId: String? = null,
    val error: AppError? = null,
)

internal class TeamCreateViewModel(
    private val createTeamUseCase: CreateTeamUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(TeamCreateUiState())
    val uiState = mutableState.asStateFlow()

    fun updateTeamName(name: String) {
        mutableState.value =
            mutableState.value.copy(
                teamName = name,
                nameError = if (name.isNotBlank()) null else mutableState.value.nameError,
            )
    }

    fun updateShortName(shortName: String) {
        val uppercase = shortName.uppercase().take(6)
        mutableState.value = mutableState.value.copy(shortName = uppercase)
    }

    fun updateDescription(desc: String) {
        mutableState.value = mutableState.value.copy(description = desc.take(300))
    }

    fun updateWard(ward: String) {
        mutableState.value = mutableState.value.copy(selectedWard = ward, wardError = null)
    }

    fun toggleSkill(skill: String) {
        val current = mutableState.value.selectedSkills.toMutableSet()
        if (current.contains(skill)) {
            if (current.size > 1) {
                current.remove(skill)
            }
        } else {
            if (current.size < 3) {
                current.add(skill)
            }
        }
        mutableState.value = mutableState.value.copy(selectedSkills = current)
    }

    fun createTeam(onSuccess: (String) -> Unit) {
        val name = mutableState.value.teamName.trim()
        val ward = mutableState.value.selectedWard.trim()

        var hasError = false
        if (name.isBlank()) {
            mutableState.value = mutableState.value.copy(nameError = "Tên đội bóng không được để trống")
            hasError = true
        }
        if (ward.isBlank()) {
            mutableState.value = mutableState.value.copy(wardError = "Vui lòng chọn khu vực hoạt động")
            hasError = true
        }

        if (hasError || mutableState.value.isSubmitting) return

        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isSubmitting = true, error = null)
            val draft =
                TeamDraft(
                    name = name,
                    shortName = mutableState.value.shortName.ifBlank { name.take(3).uppercase() },
                    description = mutableState.value.description,
                    ward = ward,
                    skillLevels = mutableState.value.selectedSkills.toList(),
                )

            when (val result = createTeamUseCase(draft)) {
                is Result.Success -> {
                    mutableState.value =
                        mutableState.value.copy(
                            isSubmitting = false,
                            isCreatedSuccessfully = true,
                            createdTeamId = result.value.id,
                        )
                    onSuccess(result.value.id)
                }

                is Result.Failure -> {
                    mutableState.value =
                        mutableState.value.copy(
                            isSubmitting = false,
                            error = result.error,
                        )
                }
            }
        }
    }
}
