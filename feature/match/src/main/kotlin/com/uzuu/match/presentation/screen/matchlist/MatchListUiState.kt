package com.uzuu.match.presentation.screen.matchlist

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.uzuu.match.domain.model.MatchSummary

internal data class MatchListUiState( // nội bộ trong module
    val matches: List<MatchSummary> = emptyList(),
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalPages: Int = 0,
    val totalItems: Int = 0,
    val isLoading: Boolean = false,
    val error: AppError? = null,
) : BaseState
