package com.uzuu.match.presentation.screen.matchlist

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchType

enum class MatchTypeFilter(val label: String) {
    ALL("Tất cả"),
    HAS_VENUE("Đã có sân"),
    NEED_VENUE("Chưa có sân"),
}

internal data class MatchListUiState(
    val matches: List<MatchSummary> = emptyList(),
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalPages: Int = 0,
    val totalItems: Int = 0,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val selectedFilter: MatchTypeFilter = MatchTypeFilter.ALL,
    val selectedDate: String? = "2026-11-21",
) : BaseState {
    val filteredMatches: List<MatchSummary>
        get() = matches.filter { match ->
            val matchesType = when (selectedFilter) {
                MatchTypeFilter.ALL -> true
                MatchTypeFilter.HAS_VENUE -> match.type == MatchType.HAS_VENUE
                MatchTypeFilter.NEED_VENUE -> match.type == MatchType.NEED_VENUE
            }
            val matchesDate = if (selectedDate.isNullOrBlank()) {
                true
            } else {
                match.startsAt.isBlank() || match.startsAt.startsWith(selectedDate)
            }
            matchesType && matchesDate
        }
}
