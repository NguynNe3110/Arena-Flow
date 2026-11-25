package com.igorwojda.showcase.feature.venue.presentation.screen.venuephotos

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.SessionUserSummary
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.venue.domain.model.VenueDetail
import com.igorwojda.showcase.feature.venue.domain.model.VenueImage
import com.igorwojda.showcase.feature.venue.domain.usecase.DeleteVenueImageUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.GetVenueDetailUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.SetPrimaryVenueImageUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.UploadVenueImageUseCase
import kotlinx.coroutines.launch

data class VenuePhotosUiState(
    val isLoading: Boolean = true,
    val venue: VenueDetail? = null,
    val images: List<VenueImage> = emptyList(),
    val currentUser: SessionUserSummary? = null,
    val errorMessage: String? = null,
    val isUploading: Boolean = false,
    val failedUploadLabel: String? = null,
    val uploadErrorMessage: String? = null,
    val busyImageId: String? = null,
    val snackbarMessage: String? = null,
    val unreadNotifications: Int = 0,
) : BaseState {
    val canUploadMore: Boolean
        get() {
            val v = venue ?: return false
            val u = currentUser ?: return false
            return images.size < MAX_PHOTOS && (!v.isInactive || u.role == AccountRole.ADMIN)
        }

    val canModifyPrimary: Boolean
        get() {
            val v = venue ?: return false
            val u = currentUser ?: return false
            return !v.isInactive || u.role == AccountRole.ADMIN
        }

    val canDeletePhotos: Boolean
        get() {
            val v = venue ?: return false
            val u = currentUser ?: return false
            return u.role == AccountRole.ADMIN || (v.createdById == u.id && !v.isInactive)
        }

    companion object {
        const val MAX_PHOTOS = 5
    }
}

sealed interface VenuePhotosAction : BaseAction<VenuePhotosUiState> {
    data object Loading : VenuePhotosAction {
        override fun reduce(state: VenuePhotosUiState) = state.copy(isLoading = true, errorMessage = null)
    }

    data class SessionUpdated(
        val user: SessionUserSummary?,
        val unreadNotifications: Int,
    ) : VenuePhotosAction {
        override fun reduce(state: VenuePhotosUiState) = state.copy(currentUser = user, unreadNotifications = unreadNotifications)
    }

    data class Loaded(
        val venue: VenueDetail,
    ) : VenuePhotosAction {
        override fun reduce(state: VenuePhotosUiState) =
            state.copy(
                isLoading = false,
                venue = venue,
                images = venue.images,
                errorMessage = null,
            )
    }

    data class Error(
        val message: String,
    ) : VenuePhotosAction {
        override fun reduce(state: VenuePhotosUiState) = state.copy(isLoading = false, errorMessage = message)
    }

    data object Uploading : VenuePhotosAction {
        override fun reduce(state: VenuePhotosUiState) = state.copy(isUploading = true, uploadErrorMessage = null)
    }

    data class UploadSucceeded(
        val image: VenueImage,
    ) : VenuePhotosAction {
        override fun reduce(state: VenuePhotosUiState) =
            state.copy(
                isUploading = false,
                failedUploadLabel = null,
                uploadErrorMessage = null,
                images =
                    if (image.isPrimary) {
                        state.images.map { it.copy(isPrimary = false) } + image
                    } else {
                        state.images + image
                    },
                snackbarMessage = "Đã tải ảnh sân lên thành công!",
            )
    }

    data class UploadFailed(
        val label: String,
        val message: String,
    ) : VenuePhotosAction {
        override fun reduce(state: VenuePhotosUiState) =
            state.copy(
                isUploading = false,
                failedUploadLabel = label,
                uploadErrorMessage = message,
            )
    }

    data class ImageBusy(
        val imageId: String,
    ) : VenuePhotosAction {
        override fun reduce(state: VenuePhotosUiState) = state.copy(busyImageId = imageId)
    }

    data class ImagesUpdated(
        val images: List<VenueImage>,
        val message: String,
    ) : VenuePhotosAction {
        override fun reduce(state: VenuePhotosUiState) = state.copy(busyImageId = null, images = images, snackbarMessage = message)
    }

    data class ImageActionFailed(
        val message: String,
    ) : VenuePhotosAction {
        override fun reduce(state: VenuePhotosUiState) = state.copy(busyImageId = null, snackbarMessage = message)
    }

    data object SnackbarConsumed : VenuePhotosAction {
        override fun reduce(state: VenuePhotosUiState) = state.copy(snackbarMessage = null)
    }
}

class VenuePhotosViewModel(
    private val deleteVenueImageUseCase: DeleteVenueImageUseCase,
    private val getSessionStateUseCase: GetSessionStateUseCase,
    private val getVenueDetailUseCase: GetVenueDetailUseCase,
    private val setPrimaryVenueImageUseCase: SetPrimaryVenueImageUseCase,
    private val uploadVenueImageUseCase: UploadVenueImageUseCase,
) : BaseViewModel<VenuePhotosUiState, VenuePhotosAction>(VenuePhotosUiState()) {
    init {
        viewModelScope.launch {
            getSessionStateUseCase().collect { session ->
                sendAction(
                    VenuePhotosAction.SessionUpdated(
                        user = session.currentUser,
                        unreadNotifications = session.unreadNotifications,
                    ),
                )
            }
        }
    }

    fun loadPhotos(venueId: String) {
        viewModelScope.launch {
            sendAction(VenuePhotosAction.Loading)
            when (val result = getVenueDetailUseCase(venueId)) {
                is Result.Success -> sendAction(VenuePhotosAction.Loaded(result.value))
                is Result.Failure -> sendAction(VenuePhotosAction.Error(result.error.userMessage()))
            }
        }
    }

    fun uploadPhoto(imageLabel: String = "Mặt sân #${uiStateFlow.value.images.size + 1}") {
        val venueId = uiStateFlow.value.venue?.venueId ?: return
        val isFirst = uiStateFlow.value.images.isEmpty()
        viewModelScope.launch {
            sendAction(VenuePhotosAction.Uploading)
            when (val result = uploadVenueImageUseCase(venueId, imageLabel, isPrimary = isFirst)) {
                is Result.Success -> {
                    sendAction(VenuePhotosAction.UploadSucceeded(result.value))
                }

                is Result.Failure -> {
                    sendAction(VenuePhotosAction.UploadFailed(imageLabel, result.error.userMessage()))
                }
            }
        }
    }

    fun retryFailedUpload() {
        val label = uiStateFlow.value.failedUploadLabel ?: return
        uploadPhoto(label)
    }

    fun setPrimary(imageId: String) {
        val venueId = uiStateFlow.value.venue?.venueId ?: return
        viewModelScope.launch {
            sendAction(VenuePhotosAction.ImageBusy(imageId))
            when (val result = setPrimaryVenueImageUseCase(venueId, imageId)) {
                is Result.Success -> {
                    sendAction(VenuePhotosAction.ImagesUpdated(result.value, "Đã đặt làm ảnh chính."))
                }

                is Result.Failure -> {
                    sendAction(VenuePhotosAction.ImageActionFailed(result.error.userMessage()))
                }
            }
        }
    }

    fun deletePhoto(imageId: String) {
        val venueId = uiStateFlow.value.venue?.venueId ?: return
        viewModelScope.launch {
            sendAction(VenuePhotosAction.ImageBusy(imageId))
            when (val result = deleteVenueImageUseCase(venueId, imageId)) {
                is Result.Success -> {
                    sendAction(VenuePhotosAction.ImagesUpdated(result.value, "Đã xóa ảnh sân."))
                }

                is Result.Failure -> {
                    sendAction(VenuePhotosAction.ImageActionFailed(result.error.userMessage()))
                }
            }
        }
    }

    fun consumeSnackbar() {
        sendAction(VenuePhotosAction.SnackbarConsumed)
    }
}
