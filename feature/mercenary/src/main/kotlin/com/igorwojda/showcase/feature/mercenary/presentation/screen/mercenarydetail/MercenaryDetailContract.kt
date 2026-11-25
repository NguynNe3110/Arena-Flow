package com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarydetail

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryApplication
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostDetail

/** UI state for [MercenaryDetailScreen]. */
internal data class MercenaryDetailUiState(
    val postId: String = "",
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val post: MercenaryPostDetail? = null,
    val applications: List<MercenaryApplication> = emptyList(),
    val showApplySheet: Boolean = false,
    val error: AppError? = null,
    val snackbarMessage: String? = null,
) : BaseState

/** User events dispatched from [MercenaryDetailScreen] to [MercenaryDetailViewModel]. */
internal sealed interface MercenaryDetailEvent {
    data class Load(
        val postId: String,
    ) : MercenaryDetailEvent

    data object OpenApplySheet : MercenaryDetailEvent

    data object DismissApplySheet : MercenaryDetailEvent

    data class SubmitApplication(
        val position: PlayerPosition,
        val note: String,
    ) : MercenaryDetailEvent

    data object WithdrawApplication : MercenaryDetailEvent

    data class AcceptApplication(
        val appId: String,
    ) : MercenaryDetailEvent

    data class RejectApplication(
        val appId: String,
    ) : MercenaryDetailEvent

    data object ClosePost : MercenaryDetailEvent

    data object ClearSnackbar : MercenaryDetailEvent
}

/** State reducers for [MercenaryDetailViewModel]. */
internal sealed interface MercenaryDetailAction : BaseAction<MercenaryDetailUiState> {
    data class SetLoading(
        val postId: String,
    ) : MercenaryDetailAction {
        override fun reduce(state: MercenaryDetailUiState) = state.copy(postId = postId, isLoading = true, error = null)
    }

    data class DetailLoaded(
        val post: MercenaryPostDetail,
        val applications: List<MercenaryApplication>,
    ) : MercenaryDetailAction {
        override fun reduce(state: MercenaryDetailUiState) =
            state.copy(isLoading = false, post = post, applications = applications, error = null)
    }

    data class SetSubmitting(
        val isSubmitting: Boolean,
    ) : MercenaryDetailAction {
        override fun reduce(state: MercenaryDetailUiState) = state.copy(isSubmitting = isSubmitting)
    }

    data class SetApplySheetVisible(
        val visible: Boolean,
    ) : MercenaryDetailAction {
        override fun reduce(state: MercenaryDetailUiState) = state.copy(showApplySheet = visible)
    }

    data class SetError(
        val error: AppError,
    ) : MercenaryDetailAction {
        override fun reduce(state: MercenaryDetailUiState) = state.copy(isLoading = false, isSubmitting = false, error = error)
    }

    data class SetSnackbar(
        val message: String?,
    ) : MercenaryDetailAction {
        override fun reduce(state: MercenaryDetailUiState) = state.copy(snackbarMessage = message)
    }
}
