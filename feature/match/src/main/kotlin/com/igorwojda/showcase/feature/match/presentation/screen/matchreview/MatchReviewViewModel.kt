package com.igorwojda.showcase.feature.match.presentation.screen.matchreview

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.match.domain.usecase.CreateMatchUseCase
import com.igorwojda.showcase.feature.match.presentation.screen.matchcreate.MatchDraftHolder
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/** ViewModel managing S6 Match Review confirmation before publishing (MATCH-06). */
internal class MatchReviewViewModel(
    private val createMatchUseCase: CreateMatchUseCase,
    private val matchDraftHolder: MatchDraftHolder,
) : BaseViewModel<MatchReviewUiState, MatchReviewAction>(MatchReviewUiState(draft = matchDraftHolder.draft.value)) {
    init {
        matchDraftHolder.draft
            .onEach { sendAction(MatchReviewAction.DraftChanged(it)) }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: MatchReviewEvent) {
        when (event) {
            MatchReviewEvent.ConfirmPost -> confirmPost()
        }
    }

    private fun confirmPost() {
        val draft = uiStateFlow.value.draft
        sendAction(MatchReviewAction.Submitting(true))
        viewModelScope.launch {
            when (val res = createMatchUseCase(draft)) {
                is Result.Success -> {
                    matchDraftHolder.reset()
                    sendAction(MatchReviewAction.Posted(res.value.matchId))
                }

                is Result.Failure -> {
                    sendAction(MatchReviewAction.Error(res.error.userMessage()))
                }
            }
        }
    }
}
