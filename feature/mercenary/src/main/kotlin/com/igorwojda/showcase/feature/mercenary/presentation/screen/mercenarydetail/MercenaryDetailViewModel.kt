@file:Suppress("LongParameterList")

package com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarydetail

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.mercenary.domain.usecase.AcceptMercenaryApplicationUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.ApplyMercenaryUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.CloseMercenaryPostUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetMercenaryPostDetailUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetPostApplicationsUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.RejectMercenaryApplicationUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.WithdrawMercenaryApplicationUseCase
import kotlinx.coroutines.launch

/** ViewModel managing mercenary post details, player apply/withdraw, and captain moderation. */
internal class MercenaryDetailViewModel(
    private val acceptMercenaryApplicationUseCase: AcceptMercenaryApplicationUseCase,
    private val applyMercenaryUseCase: ApplyMercenaryUseCase,
    private val closeMercenaryPostUseCase: CloseMercenaryPostUseCase,
    private val getMercenaryPostDetailUseCase: GetMercenaryPostDetailUseCase,
    private val getPostApplicationsUseCase: GetPostApplicationsUseCase,
    private val rejectMercenaryApplicationUseCase: RejectMercenaryApplicationUseCase,
    private val withdrawMercenaryApplicationUseCase: WithdrawMercenaryApplicationUseCase,
) : BaseViewModel<MercenaryDetailUiState, MercenaryDetailAction>(MercenaryDetailUiState()) {
    /** Handles all user events from [MercenaryDetailScreen]. */
    fun onEvent(event: MercenaryDetailEvent) {
        when (event) {
            is MercenaryDetailEvent.Load -> {
                loadDetail(event.postId)
            }

            MercenaryDetailEvent.OpenApplySheet -> {
                sendAction(MercenaryDetailAction.SetApplySheetVisible(true))
            }

            MercenaryDetailEvent.DismissApplySheet -> {
                sendAction(MercenaryDetailAction.SetApplySheetVisible(false))
            }

            is MercenaryDetailEvent.SubmitApplication -> {
                submitApplication(event.position, event.note)
            }

            MercenaryDetailEvent.WithdrawApplication -> {
                withdrawApplication()
            }

            is MercenaryDetailEvent.AcceptApplication -> {
                acceptApplication(event.appId)
            }

            is MercenaryDetailEvent.RejectApplication -> {
                rejectApplication(event.appId)
            }

            MercenaryDetailEvent.ClosePost -> {
                closePost()
            }

            MercenaryDetailEvent.ClearSnackbar -> {
                sendAction(MercenaryDetailAction.SetSnackbar(null))
            }
        }
    }

    private fun loadDetail(postId: String) {
        viewModelScope.launch {
            sendAction(MercenaryDetailAction.SetLoading(postId))
            when (val detailResult = getMercenaryPostDetailUseCase(postId)) {
                is Result.Success -> {
                    val detail = detailResult.value
                    val applications =
                        if (detail.viewerIsCaptain) {
                            when (val appsResult = getPostApplicationsUseCase(postId)) {
                                is Result.Success -> appsResult.value
                                is Result.Failure -> emptyList()
                            }
                        } else {
                            emptyList()
                        }
                    sendAction(MercenaryDetailAction.DetailLoaded(detail, applications))
                }

                is Result.Failure -> {
                    sendAction(MercenaryDetailAction.SetError(detailResult.error))
                }
            }
        }
    }

    private fun submitApplication(
        position: PlayerPosition,
        note: String,
    ) {
        val postId = uiStateFlow.value.postId
        if (postId.isBlank() || uiStateFlow.value.isSubmitting) return
        viewModelScope.launch {
            sendAction(MercenaryDetailAction.SetSubmitting(true))
            when (val result = applyMercenaryUseCase(postId, position, note)) {
                is Result.Success -> {
                    sendAction(MercenaryDetailAction.SetSubmitting(false))
                    sendAction(MercenaryDetailAction.SetApplySheetVisible(false))
                    sendAction(MercenaryDetailAction.SetSnackbar("Đã gửi đơn ứng tuyển."))
                    loadDetail(postId)
                }

                is Result.Failure -> {
                    sendAction(MercenaryDetailAction.SetSubmitting(false))
                    sendAction(MercenaryDetailAction.SetSnackbar(result.error.userMessage()))
                }
            }
        }
    }

    private fun withdrawApplication() {
        val state = uiStateFlow.value
        val appId = state.post?.myApplication?.id ?: return
        if (state.isSubmitting) return
        viewModelScope.launch {
            sendAction(MercenaryDetailAction.SetSubmitting(true))
            when (val result = withdrawMercenaryApplicationUseCase(state.postId, appId)) {
                is Result.Success -> {
                    sendAction(MercenaryDetailAction.SetSubmitting(false))
                    sendAction(MercenaryDetailAction.SetSnackbar("Đã rút đơn ứng tuyển."))
                    loadDetail(state.postId)
                }

                is Result.Failure -> {
                    sendAction(MercenaryDetailAction.SetSubmitting(false))
                    sendAction(MercenaryDetailAction.SetSnackbar(result.error.userMessage()))
                }
            }
        }
    }

    private fun acceptApplication(appId: String) {
        val state = uiStateFlow.value
        if (state.isSubmitting) return
        viewModelScope.launch {
            sendAction(MercenaryDetailAction.SetSubmitting(true))
            when (val result = acceptMercenaryApplicationUseCase(state.postId, appId)) {
                is Result.Success -> {
                    sendAction(MercenaryDetailAction.SetSubmitting(false))
                    sendAction(MercenaryDetailAction.SetSnackbar("Đã duyệt ứng viên."))
                    loadDetail(state.postId)
                }

                is Result.Failure -> {
                    sendAction(MercenaryDetailAction.SetSubmitting(false))
                    sendAction(MercenaryDetailAction.SetSnackbar(result.error.userMessage()))
                }
            }
        }
    }

    private fun rejectApplication(appId: String) {
        val state = uiStateFlow.value
        if (state.isSubmitting) return
        viewModelScope.launch {
            sendAction(MercenaryDetailAction.SetSubmitting(true))
            when (val result = rejectMercenaryApplicationUseCase(state.postId, appId)) {
                is Result.Success -> {
                    sendAction(MercenaryDetailAction.SetSubmitting(false))
                    sendAction(MercenaryDetailAction.SetSnackbar("Đã từ chối ứng viên."))
                    loadDetail(state.postId)
                }

                is Result.Failure -> {
                    sendAction(MercenaryDetailAction.SetSubmitting(false))
                    sendAction(MercenaryDetailAction.SetSnackbar(result.error.userMessage()))
                }
            }
        }
    }

    private fun closePost() {
        val state = uiStateFlow.value
        if (state.isSubmitting) return
        viewModelScope.launch {
            sendAction(MercenaryDetailAction.SetSubmitting(true))
            when (val result = closeMercenaryPostUseCase(state.postId)) {
                is Result.Success -> {
                    sendAction(MercenaryDetailAction.SetSubmitting(false))
                    sendAction(MercenaryDetailAction.SetSnackbar("Đã đóng tin tuyển."))
                    loadDetail(state.postId)
                }

                is Result.Failure -> {
                    sendAction(MercenaryDetailAction.SetSubmitting(false))
                    sendAction(MercenaryDetailAction.SetSnackbar(result.error.userMessage()))
                }
            }
        }
    }
}
