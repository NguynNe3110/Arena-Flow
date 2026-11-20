package com.uzuu.match.presentation.screen.matchreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.MatchDraft
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.usecase.CreateMatchUseCase
import com.uzuu.match.presentation.screen.matchcreate.MatchDraftHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class MatchReviewUiState(
    val draft: MatchDraft = MatchDraft(),
    val isPosting: Boolean = false,
    val postedMatch: MatchSummary? = null,
    val error: AppError? = null,
)

internal class MatchReviewViewModel(
    private val matchDraftHolder: MatchDraftHolder,
    private val createMatchUseCase: CreateMatchUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(MatchReviewUiState(draft = matchDraftHolder.draftFlow.value))
    val uiState = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            matchDraftHolder.draftFlow.collect { draft ->
                mutableState.value = mutableState.value.copy(draft = draft)
            }
        }
    }

    fun confirmPost(onSuccess: (MatchSummary) -> Unit) {
        if (mutableState.value.isPosting) return // Anti double-click guard
        mutableState.value = mutableState.value.copy(isPosting = true, error = null)

        viewModelScope.launch {
            when (val result = createMatchUseCase(mutableState.value.draft)) {
                is Result.Success -> {
                    mutableState.value = mutableState.value.copy(
                        isPosting = false,
                        postedMatch = result.value,
                    )
                    matchDraftHolder.reset()
                    onSuccess(result.value)
                }
                is Result.Failure -> {
                    mutableState.value = mutableState.value.copy(
                        isPosting = false,
                        error = result.error,
                    )
                }
            }
        }
    }
}
