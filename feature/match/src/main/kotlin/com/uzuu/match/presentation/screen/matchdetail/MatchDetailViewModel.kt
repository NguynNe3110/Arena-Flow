package com.uzuu.match.presentation.screen.matchdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.usecase.GetMatchDetailUseCase
import com.uzuu.match.domain.model.MatchDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

internal data class MatchDetailUiState(
    val isLoading: Boolean = true,
    val match: MatchSummary? = null,
    val detail: MatchDetail? = null,
    val error: AppError? = null,
    val requestTeam: String? = null,
)

internal class MatchDetailViewModel(
    private val getMatchDetailUseCase: GetMatchDetailUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val mutableState = MutableStateFlow(MatchDetailUiState())
    val uiState = mutableState.asStateFlow()

    private var loadJob: Job? = null

    fun load(matchId: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isLoading = true, error = null)
            mutableState.value = when (val result = getMatchDetailUseCase(matchId)) {
                is Result.Success -> MatchDetailUiState(
                    isLoading = false,
                    match = result.value.summary,
                    detail = result.value,
                    requestTeam = savedStateHandle["requestTeam:$matchId"],
                )
                is Result.Failure -> MatchDetailUiState(isLoading = false, error = result.error)
            }
        }
    }

    fun sendDemoRequest(team: String) {
        val match = mutableState.value.match ?: return
        if (match.status != "OPEN" || mutableState.value.requestTeam != null) return
        savedStateHandle["requestTeam:${match.matchId}"] = team
        mutableState.value = mutableState.value.copy(requestTeam = team)
    }

    fun withdrawDemoRequest() {
        val match = mutableState.value.match ?: return
        if (match.type != com.uzuu.match.domain.model.MatchType.HAS_VENUE) return
        savedStateHandle.remove<String>("requestTeam:${match.matchId}")
        mutableState.value = mutableState.value.copy(requestTeam = null)
    }
}
