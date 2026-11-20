package com.uzuu.match.presentation.screen.matchlist

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.match.domain.usecase.GetMatchUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class MatchListViewModel(
    private val getMatchUseCase: GetMatchUseCase,
) : BaseViewModel<MatchListUiState, MatchListAction>(MatchListUiState()) {
    private var loadJob: Job? = null

    init {
        onRefresh()
    }

    fun onRefresh() {
        if (loadJob?.isActive == true) return

        loadJob =
            viewModelScope.launch {
                sendAction(MatchListAction.Loading)

                when (val result = getMatchUseCase()) {
                    is Result.Success -> sendAction(
                        MatchListAction.Loaded(result.value)
                    )
                    is Result.Failure -> sendAction(
                        MatchListAction.Failed(result.error)
                    )
                }
            }
    }
}
