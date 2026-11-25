package com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarylist

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostFilter
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostSummary
import com.igorwojda.showcase.feature.mercenary.domain.usecase.ApplyMercenaryUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetMercenaryPostsUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetMyMercenaryApplicationsUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetMyTeamMercenaryPostsUseCase
import kotlinx.coroutines.launch

/** ViewModel managing the mercenary list, segmented tabs, filters, and quick apply. */
internal class MercenaryListViewModel(
    private val applyMercenaryUseCase: ApplyMercenaryUseCase,
    private val getMercenaryPostsUseCase: GetMercenaryPostsUseCase,
    private val getMyMercenaryApplicationsUseCase: GetMyMercenaryApplicationsUseCase,
    private val getMyTeamMercenaryPostsUseCase: GetMyTeamMercenaryPostsUseCase,
    private val sessionRepository: SessionRepository,
) : BaseViewModel<MercenaryListUiState, MercenaryListAction>(MercenaryListUiState()) {
    init {
        observeSession()
        loadActiveTab()
    }

    /** Handles all user events from [MercenaryListScreen]. */
    fun onEvent(event: MercenaryListEvent) {
        when (event) {
            MercenaryListEvent.Refresh -> {
                loadActiveTab()
            }

            is MercenaryListEvent.SelectTab -> {
                sendAction(MercenaryListAction.SetTab(event.tab))
                loadActiveTab()
            }

            is MercenaryListEvent.SearchQueryChanged -> {
                sendAction(MercenaryListAction.SetSearchQuery(event.query))
            }

            is MercenaryListEvent.PositionFilterChanged -> {
                sendAction(MercenaryListAction.SetPositionFilter(event.position))
                loadExplorePosts()
            }

            is MercenaryListEvent.FormatFilterChanged -> {
                sendAction(MercenaryListAction.SetFormatFilter(event.format))
                loadExplorePosts()
            }

            is MercenaryListEvent.WardFilterChanged -> {
                sendAction(MercenaryListAction.SetWardFilter(event.wardId))
                loadExplorePosts()
            }

            is MercenaryListEvent.QuickApply -> {
                quickApply(event.post)
            }

            MercenaryListEvent.ClearSnackbar -> {
                sendAction(MercenaryListAction.SetSnackbar(null))
            }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            val wards = sessionRepository.getWards()
            sessionRepository.observeSession().collect { session ->
                sendAction(
                    MercenaryListAction.SetSessionContext(
                        wards = wards,
                        isCaptain = session.captainedTeams.isNotEmpty(),
                    ),
                )
            }
        }
    }

    private fun loadActiveTab() {
        when (uiStateFlow.value.selectedTab) {
            MercenaryListTab.EXPLORE -> loadExplorePosts()
            MercenaryListTab.MY_APPLICATIONS -> loadMyApplications()
            MercenaryListTab.MY_TEAM_POSTS -> loadMyTeamPosts()
        }
    }

    private fun loadExplorePosts() {
        val current = uiStateFlow.value
        viewModelScope.launch {
            sendAction(MercenaryListAction.SetLoading(true))
            val filter =
                MercenaryPostFilter(
                    wardId = current.selectedWardId,
                    position = current.selectedPosition,
                    format = current.selectedFormat,
                )
            when (val result = getMercenaryPostsUseCase(filter)) {
                is Result.Success -> sendAction(MercenaryListAction.ExploreLoaded(result.value.items))
                is Result.Failure -> sendAction(MercenaryListAction.SetError(result.error))
            }
        }
    }

    private fun loadMyApplications() {
        viewModelScope.launch {
            sendAction(MercenaryListAction.SetLoading(true))
            when (val result = getMyMercenaryApplicationsUseCase()) {
                is Result.Success -> sendAction(MercenaryListAction.MyApplicationsLoaded(result.value))
                is Result.Failure -> sendAction(MercenaryListAction.SetError(result.error))
            }
        }
    }

    private fun loadMyTeamPosts() {
        viewModelScope.launch {
            sendAction(MercenaryListAction.SetLoading(true))
            when (val result = getMyTeamMercenaryPostsUseCase()) {
                is Result.Success -> sendAction(MercenaryListAction.MyTeamPostsLoaded(result.value.items))
                is Result.Failure -> sendAction(MercenaryListAction.SetError(result.error))
            }
        }
    }

    private fun quickApply(post: MercenaryPostSummary) {
        if (uiStateFlow.value.isSubmitting) return
        viewModelScope.launch {
            sendAction(MercenaryListAction.SetSubmitting(true))
            when (val result = applyMercenaryUseCase(post.id, post.position, "")) {
                is Result.Success -> {
                    sendAction(MercenaryListAction.SetSubmitting(false))
                    sendAction(MercenaryListAction.SetSnackbar("Đã gửi đơn ứng tuyển, chờ đội trưởng duyệt."))
                    loadExplorePosts()
                }

                is Result.Failure -> {
                    sendAction(MercenaryListAction.SetSubmitting(false))
                    sendAction(MercenaryListAction.SetSnackbar(result.error.userMessage()))
                }
            }
        }
    }
}
