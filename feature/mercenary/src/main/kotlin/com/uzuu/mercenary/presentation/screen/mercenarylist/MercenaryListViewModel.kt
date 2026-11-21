package com.uzuu.mercenary.presentation.screen.mercenarylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.mercenary.domain.model.MercenaryPost
import com.uzuu.mercenary.domain.usecase.ApplyMercenaryUseCase
import com.uzuu.mercenary.domain.usecase.GetMercenaryPostsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class MercenaryListUiState(
    val isLoading: Boolean = true,
    val posts: List<MercenaryPost> = emptyList(),
    val filterPosition: String = "GK",
    val radiusKm: Float = 3.0f,
    val selectedPostForApply: MercenaryPost? = null,
    val isApplying: Boolean = false,
    val error: AppError? = null,
)

internal class MercenaryListViewModel(
    private val getMercenaryPostsUseCase: GetMercenaryPostsUseCase,
    private val applyMercenaryUseCase: ApplyMercenaryUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(MercenaryListUiState())
    val uiState = mutableState.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isLoading = true, error = null)
            when (val result = getMercenaryPostsUseCase()) {
                is Result.Success<List<MercenaryPost>> -> {
                    mutableState.value =
                        mutableState.value.copy(
                            isLoading = false,
                            posts = result.value,
                        )
                }

                is Result.Failure -> {
                    mutableState.value =
                        mutableState.value.copy(
                            isLoading = false,
                            error = result.error,
                        )
                }
            }
        }
    }

    fun setFilterPosition(position: String) {
        mutableState.value = mutableState.value.copy(filterPosition = position)
    }

    fun setRadius(radiusKm: Float) {
        mutableState.value = mutableState.value.copy(radiusKm = radiusKm)
    }

    fun selectPostForApply(post: MercenaryPost?) {
        mutableState.value = mutableState.value.copy(selectedPostForApply = post)
    }

    fun applyToPost(
        postId: String,
        message: String = "",
    ) {
        if (mutableState.value.isApplying) return
        mutableState.value = mutableState.value.copy(isApplying = true)

        viewModelScope.launch {
            when (val result = applyMercenaryUseCase(postId, message)) {
                is Result.Success<MercenaryPost> -> {
                    val updatedPosts =
                        mutableState.value.posts.map {
                            if (it.id == postId) result.value else it
                        }
                    mutableState.value =
                        mutableState.value.copy(
                            isApplying = false,
                            posts = updatedPosts,
                            selectedPostForApply = null,
                        )
                }

                is Result.Failure -> {
                    mutableState.value =
                        mutableState.value.copy(
                            isApplying = false,
                            error = result.error,
                        )
                }
            }
        }
    }
}
