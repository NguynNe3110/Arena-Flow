@file:Suppress("MagicNumber")

package com.igorwojda.showcase.feature.team.presentation.screen.teamform

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.team.domain.model.OfficialSkillLabels
import com.igorwojda.showcase.feature.team.domain.model.TeamDraft
import com.igorwojda.showcase.feature.team.domain.usecase.CreateTeamUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetTeamDetailUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.UpdateTeamUseCase
import kotlinx.coroutines.launch

data class TeamFormUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val teamName: String = "",
    val description: String = "",
    val selectedSkills: List<String> = listOf("Nhiệt tình", "Rèn luyện"),
    val nameError: String? = null,
    val skillError: String? = null,
    val formError: String? = null,
) : BaseState

sealed interface TeamFormAction : BaseAction<TeamFormUiState> {
    data object Loading : TeamFormAction {
        override fun reduce(state: TeamFormUiState): TeamFormUiState = state.copy(isLoading = true, formError = null)
    }

    data class LoadedForEdit(
        val name: String,
        val description: String,
        val skills: List<String>,
    ) : TeamFormAction {
        override fun reduce(state: TeamFormUiState): TeamFormUiState =
            state.copy(
                isEditMode = true,
                isLoading = false,
                teamName = name,
                description = description,
                selectedSkills = skills,
                formError = null,
            )
    }

    data class UpdateName(
        val value: String,
    ) : TeamFormAction {
        override fun reduce(state: TeamFormUiState): TeamFormUiState =
            state.copy(teamName = value.take(100), nameError = null, formError = null)
    }

    data class UpdateDescription(
        val value: String,
    ) : TeamFormAction {
        override fun reduce(state: TeamFormUiState): TeamFormUiState = state.copy(description = value.take(1000), formError = null)
    }

    data class UpdateSkills(
        val skills: List<String>,
        val error: String? = null,
    ) : TeamFormAction {
        override fun reduce(state: TeamFormUiState): TeamFormUiState = state.copy(selectedSkills = skills, skillError = error)
    }

    data class ValidationError(
        val nameError: String? = null,
        val skillError: String? = null,
    ) : TeamFormAction {
        override fun reduce(state: TeamFormUiState): TeamFormUiState =
            state.copy(isSubmitting = false, nameError = nameError, skillError = skillError)
    }

    data object Submitting : TeamFormAction {
        override fun reduce(state: TeamFormUiState): TeamFormUiState = state.copy(isSubmitting = true, formError = null)
    }

    data class SubmitError(
        val message: String,
        val nameError: String? = null,
    ) : TeamFormAction {
        override fun reduce(state: TeamFormUiState): TeamFormUiState =
            state.copy(isLoading = false, isSubmitting = false, formError = message, nameError = nameError)
    }

    data object Submitted : TeamFormAction {
        override fun reduce(state: TeamFormUiState): TeamFormUiState = state.copy(isSubmitting = false)
    }
}

internal class TeamFormViewModel(
    private val createTeamUseCase: CreateTeamUseCase,
    private val getTeamDetailUseCase: GetTeamDetailUseCase,
    private val updateTeamUseCase: UpdateTeamUseCase,
) : BaseViewModel<TeamFormUiState, TeamFormAction>(TeamFormUiState()) {
    private var editingTeamId: String? = null

    fun init(teamId: String?) {
        editingTeamId = teamId
        if (teamId.isNullOrBlank()) return
        viewModelScope.launch {
            sendAction(TeamFormAction.Loading)
            when (val result = getTeamDetailUseCase(teamId)) {
                is Result.Success -> {
                    val team = result.value
                    sendAction(
                        TeamFormAction.LoadedForEdit(
                            name = team.teamName,
                            description = team.description.orEmpty(),
                            skills = team.skillLevels.filter { it in OfficialSkillLabels.all }.take(3),
                        ),
                    )
                }

                is Result.Failure -> {
                    sendAction(TeamFormAction.SubmitError(result.error.userMessage()))
                }
            }
        }
    }

    fun onNameChange(value: String) {
        sendAction(TeamFormAction.UpdateName(value))
    }

    fun onDescriptionChange(value: String) {
        sendAction(TeamFormAction.UpdateDescription(value))
    }

    fun toggleSkill(label: String) {
        val current = uiStateFlow.value.selectedSkills
        if (label in current) {
            sendAction(TeamFormAction.UpdateSkills(current.filterNot { it == label }, null))
        } else if (current.size >= 3) {
            sendAction(TeamFormAction.UpdateSkills(current, "Chỉ được chọn tối đa 3 nhãn trình độ"))
        } else {
            sendAction(TeamFormAction.UpdateSkills(current + label, null))
        }
    }

    fun submit(onSuccess: (teamId: String) -> Unit) {
        val current = uiStateFlow.value
        if (current.isSubmitting) return

        val cleanName = current.teamName.trim()
        val nameErr = if (cleanName.isEmpty()) "Tên đội không được để trống" else null
        val skillErr = if (current.selectedSkills.isEmpty()) "Vui lòng chọn 1 đến 3 nhãn trình độ" else null

        if (nameErr != null || skillErr != null) {
            sendAction(TeamFormAction.ValidationError(nameError = nameErr, skillError = skillErr))
            return
        }

        val draft =
            TeamDraft(
                teamName = cleanName,
                description = current.description.trim().ifBlank { null },
                skillLevels = current.selectedSkills,
            )

        viewModelScope.launch {
            sendAction(TeamFormAction.Submitting)
            val targetId = editingTeamId
            val result =
                if (targetId.isNullOrBlank()) {
                    createTeamUseCase(draft)
                } else {
                    updateTeamUseCase(targetId, draft)
                }
            when (result) {
                is Result.Success -> {
                    sendAction(TeamFormAction.Submitted)
                    onSuccess(result.value.teamId)
                }

                is Result.Failure -> {
                    val domainErr = result.error as? AppError.DomainError
                    sendAction(
                        TeamFormAction.SubmitError(
                            message = result.error.userMessage(),
                            nameError = domainErr?.fieldErrors?.get("teamName"),
                        ),
                    )
                }
            }
        }
    }
}
