package com.uzuu.match.presentation.screen.matchlist

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.match.domain.model.MatchPage

internal sealed interface MatchListAction : BaseAction<MatchListUiState> {
    data object Loading : MatchListAction {
        override fun reduce(state: MatchListUiState): MatchListUiState = state.copy(isLoading = true, error = null)
    }

    data class Loaded(
        val matchPage: MatchPage,
    ) : MatchListAction {
        override fun reduce(state: MatchListUiState): MatchListUiState =
            state.copy(
                matches = matchPage.matches,
                page = matchPage.page,
                pageSize = matchPage.pageSize,
                totalPages = matchPage.totalPages,
                totalItems = matchPage.totalItems,
                isLoading = false,
                error = null,
            )
    }

    data class Failed(
        val error: AppError,
    ) : MatchListAction {
        override fun reduce(state: MatchListUiState): MatchListUiState = state.copy(isLoading = false, error = error)
    }
}
