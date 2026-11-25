package com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarylist

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.model.WardOption
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryApplication
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostSummary

/** Tabs available on the MercenaryListScreen. */
enum class MercenaryListTab(
    val label: String,
) {
    EXPLORE("Tin tuyển lính"),
    MY_APPLICATIONS("Đơn của tôi"),
    MY_TEAM_POSTS("Tin đội mình"),
}

/** UI state for [MercenaryListScreen]. */
internal data class MercenaryListUiState(
    val selectedTab: MercenaryListTab = MercenaryListTab.EXPLORE,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val searchQuery: String = "",
    val selectedPosition: PlayerPosition? = null,
    val selectedFormat: PitchFormat? = null,
    val selectedWardId: String? = null,
    val wards: List<WardOption> = emptyList(),
    val explorePosts: List<MercenaryPostSummary> = emptyList(),
    val myApplications: List<MercenaryApplication> = emptyList(),
    val myTeamPosts: List<MercenaryPostSummary> = emptyList(),
    val isCaptain: Boolean = false,
    val error: AppError? = null,
    val snackbarMessage: String? = null,
) : BaseState

/** User events dispatched from [MercenaryListScreen] to [MercenaryListViewModel]. */
internal sealed interface MercenaryListEvent {
    data object Refresh : MercenaryListEvent

    data class SelectTab(
        val tab: MercenaryListTab,
    ) : MercenaryListEvent

    data class SearchQueryChanged(
        val query: String,
    ) : MercenaryListEvent

    data class PositionFilterChanged(
        val position: PlayerPosition?,
    ) : MercenaryListEvent

    data class FormatFilterChanged(
        val format: PitchFormat?,
    ) : MercenaryListEvent

    data class WardFilterChanged(
        val wardId: String?,
    ) : MercenaryListEvent

    data class QuickApply(
        val post: MercenaryPostSummary,
    ) : MercenaryListEvent

    data object ClearSnackbar : MercenaryListEvent
}

/** State reducers for [MercenaryListViewModel]. */
internal sealed interface MercenaryListAction : BaseAction<MercenaryListUiState> {
    data class SetLoading(
        val isLoading: Boolean,
    ) : MercenaryListAction {
        override fun reduce(state: MercenaryListUiState) = state.copy(isLoading = isLoading, error = if (isLoading) null else state.error)
    }

    data class SetTab(
        val tab: MercenaryListTab,
    ) : MercenaryListAction {
        override fun reduce(state: MercenaryListUiState) = state.copy(selectedTab = tab)
    }

    data class SetSearchQuery(
        val query: String,
    ) : MercenaryListAction {
        override fun reduce(state: MercenaryListUiState) = state.copy(searchQuery = query)
    }

    data class SetPositionFilter(
        val position: PlayerPosition?,
    ) : MercenaryListAction {
        override fun reduce(state: MercenaryListUiState) = state.copy(selectedPosition = position)
    }

    data class SetFormatFilter(
        val format: PitchFormat?,
    ) : MercenaryListAction {
        override fun reduce(state: MercenaryListUiState) = state.copy(selectedFormat = format)
    }

    data class SetWardFilter(
        val wardId: String?,
    ) : MercenaryListAction {
        override fun reduce(state: MercenaryListUiState) = state.copy(selectedWardId = wardId)
    }

    data class SetSessionContext(
        val wards: List<WardOption>,
        val isCaptain: Boolean,
    ) : MercenaryListAction {
        override fun reduce(state: MercenaryListUiState) = state.copy(wards = wards, isCaptain = isCaptain)
    }

    data class ExploreLoaded(
        val posts: List<MercenaryPostSummary>,
    ) : MercenaryListAction {
        override fun reduce(state: MercenaryListUiState) = state.copy(isLoading = false, explorePosts = posts, error = null)
    }

    data class MyApplicationsLoaded(
        val applications: List<MercenaryApplication>,
    ) : MercenaryListAction {
        override fun reduce(state: MercenaryListUiState) = state.copy(isLoading = false, myApplications = applications, error = null)
    }

    data class MyTeamPostsLoaded(
        val posts: List<MercenaryPostSummary>,
    ) : MercenaryListAction {
        override fun reduce(state: MercenaryListUiState) = state.copy(isLoading = false, myTeamPosts = posts, error = null)
    }

    data class SetSubmitting(
        val isSubmitting: Boolean,
    ) : MercenaryListAction {
        override fun reduce(state: MercenaryListUiState) = state.copy(isSubmitting = isSubmitting)
    }

    data class SetError(
        val error: AppError,
    ) : MercenaryListAction {
        override fun reduce(state: MercenaryListUiState) = state.copy(isLoading = false, isSubmitting = false, error = error)
    }

    data class SetSnackbar(
        val message: String?,
    ) : MercenaryListAction {
        override fun reduce(state: MercenaryListUiState) = state.copy(snackbarMessage = message)
    }
}
