@file:Suppress("CognitiveComplexMethod", "ComplexCondition", "CyclomaticComplexMethod", "LabeledExpression", "LongMethod")

package com.igorwojda.showcase.feature.reputation.presentation.screen.postmatchreport

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.reputation.domain.model.MatchRating
import com.igorwojda.showcase.feature.reputation.domain.model.PostMatchOutcome
import com.igorwojda.showcase.feature.reputation.domain.model.ReportType
import com.igorwojda.showcase.feature.reputation.domain.model.SubmitRatingParams
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReport
import com.igorwojda.showcase.feature.reputation.domain.usecase.GetMatchRatingsUseCase
import com.igorwojda.showcase.feature.reputation.domain.usecase.SubmitMatchRatingUseCase
import com.igorwojda.showcase.feature.reputation.domain.usecase.SubmitMatchReportUseCase
import kotlinx.coroutines.launch

/** ViewModel for S13 Post-Match Report & Rating screen (REP-01, REP-02, REP-06). */
class PostMatchReportViewModel(
    private val getMatchRatingsUseCase: GetMatchRatingsUseCase,
    private val submitMatchRatingUseCase: SubmitMatchRatingUseCase,
    private val submitMatchReportUseCase: SubmitMatchReportUseCase,
) : BaseViewModel<PostMatchReportUiState, PostMatchReportAction>(PostMatchReportUiState()) {
    fun onEvent(event: PostMatchReportEvent) {
        when (event) {
            is PostMatchReportEvent.Load -> {
                loadRatings(event.matchId, event.teamId)
            }

            is PostMatchReportEvent.SelectOutcome -> {
                handleOutcomeSelected(event.outcome)
            }

            is PostMatchReportEvent.SelectStarRating -> {
                sendAction(PostMatchReportAction.UpdateForm { it.copy(starRating = event.stars.coerceIn(MIN_STARS, MAX_STARS)) })
            }

            is PostMatchReportEvent.ToggleFairPlayTag -> {
                toggleTag(event.tag)
            }

            is PostMatchReportEvent.UpdateRaterScore -> {
                sendAction(PostMatchReportAction.UpdateForm { it.copy(raterTeamScoreInput = event.value.filter(Char::isDigit)) })
            }

            is PostMatchReportEvent.UpdateRateeScore -> {
                sendAction(PostMatchReportAction.UpdateForm { it.copy(rateeTeamScoreInput = event.value.filter(Char::isDigit)) })
            }

            is PostMatchReportEvent.UpdateComment -> {
                sendAction(PostMatchReportAction.UpdateForm { it.copy(comment = event.value.take(MAX_COMMENT_LENGTH)) })
            }

            is PostMatchReportEvent.SetAbuseReportSheetOpen -> {
                sendAction(PostMatchReportAction.UpdateForm { it.copy(isAbuseReportSheetOpen = event.open) })
            }

            is PostMatchReportEvent.SelectAbuseReportType -> {
                sendAction(PostMatchReportAction.UpdateForm { it.copy(abuseReportType = event.reportType) })
            }

            is PostMatchReportEvent.UpdateAbuseReason -> {
                sendAction(PostMatchReportAction.UpdateForm { it.copy(abuseReason = event.reason.take(MAX_REASON_LENGTH)) })
            }

            PostMatchReportEvent.ConfirmAbuseReportDraft -> {
                confirmAbuseDraft()
            }

            PostMatchReportEvent.ClearAbuseReportDraft -> {
                sendAction(
                    PostMatchReportAction.UpdateForm {
                        it.copy(includeAbuseReport = false, isAbuseReportSheetOpen = false, abuseReason = "")
                    },
                )
            }

            PostMatchReportEvent.Submit -> {
                submit()
            }
        }
    }

    private fun loadRatings(
        matchId: String,
        teamId: String,
    ) {
        sendAction(PostMatchReportAction.Loading(matchId = matchId, teamId = teamId))
        viewModelScope.launch {
            when (val result = getMatchRatingsUseCase(matchId = matchId, teamId = teamId)) {
                is Result.Success -> sendAction(PostMatchReportAction.Loaded(result.value))
                is Result.Failure -> sendAction(PostMatchReportAction.LoadError(result.error))
            }
        }
    }

    private fun handleOutcomeSelected(outcome: PostMatchOutcome) {
        sendAction(
            PostMatchReportAction.UpdateForm { state ->
                val defaultReportType =
                    when (outcome) {
                        PostMatchOutcome.OPPONENT_NO_SHOW -> ReportType.NOSHOW
                        PostMatchOutcome.CANCELLED -> ReportType.BAD_COMMUNICATION
                        else -> state.abuseReportType
                    }
                state.copy(outcome = outcome, abuseReportType = defaultReportType)
            },
        )
    }

    private fun toggleTag(tag: String) {
        sendAction(
            PostMatchReportAction.UpdateForm { state ->
                val updated = if (tag in state.selectedTags) state.selectedTags - tag else state.selectedTags + tag
                state.copy(selectedTags = updated)
            },
        )
    }

    private fun confirmAbuseDraft() {
        val reason = uiStateFlow.value.abuseReason.trim()
        if (reason.isEmpty()) {
            sendAction(PostMatchReportAction.ValidationFailed("Vui lòng nhập lý do báo cáo vi phạm."))
            return
        }
        sendAction(
            PostMatchReportAction.UpdateForm {
                it.copy(includeAbuseReport = true, isAbuseReportSheetOpen = false)
            },
        )
    }

    private fun submit() {
        val state = uiStateFlow.value
        if (state.isSubmitting || state.matchId.isBlank()) return

        val isRatingFlow = state.outcome == PostMatchOutcome.COMPLETED || state.outcome == PostMatchOutcome.OPPONENT_NO_SHOW
        val isNoShow = state.outcome == PostMatchOutcome.OPPONENT_NO_SHOW

        val raterScore = state.raterTeamScoreInput.toIntOrNull()
        val rateeScore = state.rateeTeamScoreInput.toIntOrNull()
        if (!isNoShow && ((raterScore == null) != (rateeScore == null))) {
            sendAction(PostMatchReportAction.ValidationFailed("Vui lòng nhập đủ tỷ số của cả hai đội hoặc để trống."))
            return
        }
        if ((state.outcome == PostMatchOutcome.DISPUTED || state.outcome == PostMatchOutcome.CANCELLED || state.includeAbuseReport) &&
            state.abuseReason.trim().isEmpty()
        ) {
            sendAction(PostMatchReportAction.ValidationFailed("Vui lòng nhập lý do báo cáo vi phạm."))
            return
        }

        sendAction(PostMatchReportAction.Submitting)
        viewModelScope.launch {
            var submittedRating: MatchRating? = null
            if (isRatingFlow && state.existingRatings?.given == null) {
                val combinedComment = buildComment(state.selectedTags, state.comment)
                val ratingParams =
                    SubmitRatingParams(
                        matchId = state.matchId,
                        isNoShow = isNoShow,
                        score = if (isNoShow) null else state.starRating,
                        comment = combinedComment,
                        raterTeamScore = if (isNoShow) null else raterScore,
                        rateeTeamScore = if (isNoShow) null else rateeScore,
                    )
                when (val ratingResult = submitMatchRatingUseCase(ratingParams)) {
                    is Result.Success -> {
                        submittedRating = ratingResult.value
                    }

                    is Result.Failure -> {
                        sendAction(PostMatchReportAction.SubmitError(ratingResult.error))
                        return@launch
                    }
                }
            }

            var submittedReport: TeamReport? = null
            val shouldSubmitReport =
                state.includeAbuseReport ||
                    state.outcome == PostMatchOutcome.DISPUTED ||
                    state.outcome == PostMatchOutcome.CANCELLED
            if (shouldSubmitReport) {
                when (
                    val reportResult =
                        submitMatchReportUseCase(
                            matchId = state.matchId,
                            reportType = state.abuseReportType,
                            reason = state.abuseReason.trim(),
                        )
                ) {
                    is Result.Success -> {
                        submittedReport = reportResult.value
                    }

                    is Result.Failure -> {
                        sendAction(PostMatchReportAction.SubmitError(reportResult.error))
                        return@launch
                    }
                }
            }

            sendAction(PostMatchReportAction.SubmitSuccess(rating = submittedRating, report = submittedReport))
        }
    }

    private fun buildComment(
        tags: Set<String>,
        rawComment: String,
    ): String? {
        val tagPrefix = if (tags.isNotEmpty()) tags.joinToString(", ") else ""
        val trimmed = rawComment.trim()
        return listOf(tagPrefix, trimmed)
            .filter { it.isNotBlank() }
            .joinToString(" - ")
            .take(MAX_COMMENT_LENGTH)
            .ifBlank { null }
    }

    private companion object {
        const val MIN_STARS = 1
        const val MAX_STARS = 5
        const val MAX_COMMENT_LENGTH = 500
        const val MAX_REASON_LENGTH = 1000
    }
}
