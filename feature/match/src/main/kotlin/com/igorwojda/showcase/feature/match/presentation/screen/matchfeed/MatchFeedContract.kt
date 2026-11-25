package com.igorwojda.showcase.feature.match.presentation.screen.matchfeed

import com.igorwojda.showcase.feature.base.domain.model.ActingTeamOption
import com.igorwojda.showcase.feature.base.domain.model.WardOption
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.match.domain.model.MatchFilter
import com.igorwojda.showcase.feature.match.domain.model.MatchSummary
import com.igorwojda.showcase.feature.match.domain.model.MatchType

/** Tab modes on the Match Feed screen. */
enum class MatchFeedTab(
    val label: String,
) {
    DISCOVER("Tìm kèo"),
    MY_MATCHES("Kèo của đội"),
}

/** UI state for the Match Feed screen. */
internal data class MatchFeedUiState(
    val activeTab: MatchFeedTab = MatchFeedTab.DISCOVER,
    val matches: List<MatchSummary> = emptyList(),
    val myMatches: List<MatchSummary> = emptyList(),
    val filter: MatchFilter = MatchFilter(),
    val wards: List<WardOption> = emptyList(),
    val actingTeam: ActingTeamOption? = null,
    val captainedTeams: List<ActingTeamOption> = emptyList(),
    val unreadNotifications: Int = 0,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
) : BaseState {
    val visibleMatches: List<MatchSummary>
        get() {
            val base = if (activeTab == MatchFeedTab.DISCOVER) matches else myMatches
            val q = filter.query.trim()
            return if (q.isBlank()) {
                base
            } else {
                base.filter {
                    it.authorTeamName.contains(q, ignoreCase = true) ||
                        (it.venueName?.contains(q, ignoreCase = true) == true) ||
                        (it.wardName?.contains(q, ignoreCase = true) == true)
                }
            }
        }

    val activeFilterCount: Int
        get() =
            listOfNotNull(
                filter.type,
                filter.format,
                filter.wardId,
                filter.skillLevel,
                filter.dateFrom,
            ).size
}

/** User intents dispatched to MatchFeedViewModel. */
internal sealed interface MatchFeedEvent {
    data object Refresh : MatchFeedEvent

    data class SelectTab(
        val tab: MatchFeedTab,
    ) : MatchFeedEvent

    data class QueryChanged(
        val query: String,
    ) : MatchFeedEvent

    data class ApplyFilter(
        val filter: MatchFilter,
    ) : MatchFeedEvent

    data class SelectQuickType(
        val type: MatchType?,
    ) : MatchFeedEvent

    data class SelectQuickFormat(
        val format: String?,
    ) : MatchFeedEvent

    data object ResetFilter : MatchFeedEvent

    data class SelectActingTeam(
        val teamId: String,
    ) : MatchFeedEvent

    data class QuickSendRequest(
        val matchId: String,
    ) : MatchFeedEvent

    data object ConsumeSnackbar : MatchFeedEvent
}

/** State reducers for MatchFeedViewModel. */
internal sealed interface MatchFeedAction : BaseAction<MatchFeedUiState> {
    data class SessionUpdated(
        val actingTeam: ActingTeamOption?,
        val captainedTeams: List<ActingTeamOption>,
        val wards: List<WardOption>,
        val unreadNotifications: Int,
    ) : MatchFeedAction {
        override fun reduce(state: MatchFeedUiState): MatchFeedUiState =
            state.copy(
                actingTeam = actingTeam,
                captainedTeams = captainedTeams,
                wards = wards,
                unreadNotifications = unreadNotifications,
            )
    }

    data class TabChanged(
        val tab: MatchFeedTab,
    ) : MatchFeedAction {
        override fun reduce(state: MatchFeedUiState): MatchFeedUiState = state.copy(activeTab = tab)
    }

    data class FilterUpdated(
        val filter: MatchFilter,
    ) : MatchFeedAction {
        override fun reduce(state: MatchFeedUiState): MatchFeedUiState = state.copy(filter = filter)
    }

    data class Loading(
        val refreshing: Boolean,
    ) : MatchFeedAction {
        override fun reduce(state: MatchFeedUiState): MatchFeedUiState =
            state.copy(
                isLoading = !refreshing && state.matches.isEmpty(),
                isRefreshing = refreshing,
                errorMessage = null,
            )
    }

    data class FeedLoaded(
        val items: List<MatchSummary>,
    ) : MatchFeedAction {
        override fun reduce(state: MatchFeedUiState): MatchFeedUiState =
            state.copy(matches = items, isLoading = false, isRefreshing = false, errorMessage = null)
    }

    data class MyMatchesLoaded(
        val items: List<MatchSummary>,
    ) : MatchFeedAction {
        override fun reduce(state: MatchFeedUiState): MatchFeedUiState =
            state.copy(myMatches = items, isLoading = false, isRefreshing = false, errorMessage = null)
    }

    data class LoadError(
        val message: String,
    ) : MatchFeedAction {
        override fun reduce(state: MatchFeedUiState): MatchFeedUiState =
            state.copy(isLoading = false, isRefreshing = false, errorMessage = message)
    }

    data class Snackbar(
        val message: String?,
    ) : MatchFeedAction {
        override fun reduce(state: MatchFeedUiState): MatchFeedUiState = state.copy(snackbarMessage = message)
    }
}
