package com.igorwojda.showcase.feature.reputation.presentation.screen.postmatchreport

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.reputation.domain.model.MatchRating
import com.igorwojda.showcase.feature.reputation.domain.model.MatchRatings
import com.igorwojda.showcase.feature.reputation.domain.model.PostMatchOutcome
import com.igorwojda.showcase.feature.reputation.domain.model.ReportType
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReport

data class PostMatchReportUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val matchId: String = "",
    val teamId: String = "",
    val existingRatings: MatchRatings? = null,
    val outcome: PostMatchOutcome = PostMatchOutcome.COMPLETED,
    val starRating: Int = DEFAULT_STAR_RATING,
    val selectedTags: Set<String> = emptySet(),
    val raterTeamScoreInput: String = "",
    val rateeTeamScoreInput: String = "",
    val comment: String = "",
    val isAbuseReportSheetOpen: Boolean = false,
    val includeAbuseReport: Boolean = false,
    val abuseReportType: ReportType = ReportType.BAD_PLAY,
    val abuseReason: String = "",
    val submittedRating: MatchRating? = null,
    val submittedReport: TeamReport? = null,
    val validationMessage: String? = null,
    val error: AppError? = null,
) : BaseState {
    private companion object {
        const val DEFAULT_STAR_RATING = 5
    }
}

sealed interface PostMatchReportEvent {
    data class Load(
        val matchId: String,
        val teamId: String,
    ) : PostMatchReportEvent

    data class SelectOutcome(
        val outcome: PostMatchOutcome,
    ) : PostMatchReportEvent

    data class SelectStarRating(
        val stars: Int,
    ) : PostMatchReportEvent

    data class ToggleFairPlayTag(
        val tag: String,
    ) : PostMatchReportEvent

    data class UpdateRaterScore(
        val value: String,
    ) : PostMatchReportEvent

    data class UpdateRateeScore(
        val value: String,
    ) : PostMatchReportEvent

    data class UpdateComment(
        val value: String,
    ) : PostMatchReportEvent

    data class SetAbuseReportSheetOpen(
        val open: Boolean,
    ) : PostMatchReportEvent

    data class SelectAbuseReportType(
        val reportType: ReportType,
    ) : PostMatchReportEvent

    data class UpdateAbuseReason(
        val reason: String,
    ) : PostMatchReportEvent

    data object ConfirmAbuseReportDraft : PostMatchReportEvent

    data object ClearAbuseReportDraft : PostMatchReportEvent

    data object Submit : PostMatchReportEvent
}

sealed interface PostMatchReportAction : BaseAction<PostMatchReportUiState> {
    data class Loading(
        val matchId: String,
        val teamId: String,
    ) : PostMatchReportAction {
        override fun reduce(state: PostMatchReportUiState): PostMatchReportUiState =
            state.copy(
                isLoading = true,
                matchId = matchId,
                teamId = teamId,
                error = null,
                validationMessage = null,
            )
    }

    data class Loaded(
        val ratings: MatchRatings,
    ) : PostMatchReportAction {
        override fun reduce(state: PostMatchReportUiState): PostMatchReportUiState =
            state.copy(
                isLoading = false,
                existingRatings = ratings,
                error = null,
            )
    }

    data class LoadError(
        val error: AppError,
    ) : PostMatchReportAction {
        override fun reduce(state: PostMatchReportUiState): PostMatchReportUiState = state.copy(isLoading = false, error = error)
    }

    data class UpdateForm(
        val transform: (PostMatchReportUiState) -> PostMatchReportUiState,
    ) : PostMatchReportAction {
        override fun reduce(state: PostMatchReportUiState): PostMatchReportUiState =
            transform(state).copy(validationMessage = null, error = null)
    }

    data class ValidationFailed(
        val message: String,
    ) : PostMatchReportAction {
        override fun reduce(state: PostMatchReportUiState): PostMatchReportUiState =
            state.copy(isSubmitting = false, validationMessage = message)
    }

    data object Submitting : PostMatchReportAction {
        override fun reduce(state: PostMatchReportUiState): PostMatchReportUiState =
            state.copy(isSubmitting = true, error = null, validationMessage = null)
    }

    data class SubmitSuccess(
        val rating: MatchRating?,
        val report: TeamReport?,
    ) : PostMatchReportAction {
        override fun reduce(state: PostMatchReportUiState): PostMatchReportUiState =
            state.copy(
                isSubmitting = false,
                submittedRating = rating ?: state.submittedRating,
                submittedReport = report ?: state.submittedReport,
                existingRatings =
                    if (rating != null && state.existingRatings != null) {
                        state.existingRatings.copy(given = rating)
                    } else {
                        state.existingRatings
                    },
            )
    }

    data class SubmitError(
        val error: AppError,
    ) : PostMatchReportAction {
        override fun reduce(state: PostMatchReportUiState): PostMatchReportUiState = state.copy(isSubmitting = false, error = error)
    }
}
