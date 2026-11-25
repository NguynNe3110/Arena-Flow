@file:Suppress("LabeledExpression", "MagicNumber")

package com.igorwojda.showcase.feature.match.presentation.screen.matchcreate

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.match.domain.model.MatchDraft
import com.igorwojda.showcase.feature.match.domain.model.MatchType
import com.igorwojda.showcase.feature.match.domain.model.MatchVenue
import com.igorwojda.showcase.feature.match.domain.usecase.CreateMatchUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.GetMatchDetailUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.UpdateMatchUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/** ViewModel managing S5 Match Create and Edit flow with VenuePicker result consumption. */
@Suppress("LongParameterList")
internal class MatchCreateViewModel(
    private val createMatchUseCase: CreateMatchUseCase,
    private val getMatchDetailUseCase: GetMatchDetailUseCase,
    private val getSessionStateUseCase: GetSessionStateUseCase,
    private val matchDraftHolder: MatchDraftHolder,
    private val sessionRepository: SessionRepository,
    private val updateMatchUseCase: UpdateMatchUseCase,
) : BaseViewModel<MatchCreateUiState, MatchCreateAction>(MatchCreateUiState()) {
    init {
        getSessionStateUseCase()
            .onEach { session ->
                val team = session.actingTeam
                if (team != null &&
                    uiStateFlow.value.draft.hostTeamId
                        .isBlank()
                ) {
                    val updated = uiStateFlow.value.draft.copy(hostTeamId = team.id, hostTeamName = team.name)
                    matchDraftHolder.set(updated)
                    sendAction(MatchCreateAction.DraftUpdated(updated))
                }
            }.launchIn(viewModelScope)
    }

    fun onEvent(event: MatchCreateEvent) {
        when (event) {
            is MatchCreateEvent.Init -> {
                initScreen(event.matchId)
            }

            MatchCreateEvent.ConsumePendingVenue -> {
                consumePendingVenue()
            }

            is MatchCreateEvent.UpdateDraft -> {
                matchDraftHolder.update(event.transform)
                sendAction(MatchCreateAction.DraftUpdated(matchDraftHolder.draft.value))
            }

            MatchCreateEvent.Submit -> {
                submitDraft()
            }

            MatchCreateEvent.ValidateForReview -> {
                validateDraft()
            }

            MatchCreateEvent.ConsumeValidationError -> {
                sendAction(MatchCreateAction.ValidationError(null))
            }
        }
    }

    private fun initScreen(matchId: String?) {
        if (matchId.isNullOrBlank()) {
            sendAction(MatchCreateAction.DraftLoaded(matchDraftHolder.draft.value, isEditMode = false))
            return
        }
        viewModelScope.launch {
            when (val res = getMatchDetailUseCase(matchId)) {
                is Result.Success -> {
                    val d = res.value
                    val draft =
                        MatchDraft(
                            matchId = d.matchId,
                            kind = d.type,
                            hostTeamId = d.authorTeam.teamId,
                            hostTeamName = d.authorTeam.teamName,
                            sportId = d.sportId,
                            format = d.format,
                            skillLevel = d.skillLevels.firstOrNull() ?: "Rèn luyện",
                            venueId = d.venue?.venueId,
                            venueName = d.venue?.venueName,
                            venueAddress = d.venue?.address,
                            matchDate = d.startsAt.take(10),
                            startTime = d.startsAt.drop(11).take(5),
                            endTime = d.endsAt.drop(11).take(5),
                            expiresAt = d.expiresAt,
                            proposedVenues = d.venueOptions,
                            proposedTimeSlots = d.timeOptions,
                            costType = d.costAgreement.costType,
                            costSplit = d.costAgreement.costSplit ?: "FIFTY_FIFTY",
                            venueAmount = d.costAgreement.venueAmount ?: 0,
                            drinksAmount = d.costAgreement.drinksAmount,
                            depositAmount = d.costAgreement.depositAmount,
                            depositHolder = d.costAgreement.depositHolder,
                            ageNote = d.ageNote,
                            notes = d.matchNature.orEmpty(),
                            minReputationScore = d.minReputationScore,
                        )
                    matchDraftHolder.set(draft)
                    sendAction(MatchCreateAction.DraftLoaded(draft, isEditMode = true))
                }

                is Result.Failure -> {
                    sendAction(MatchCreateAction.ValidationError(res.error.userMessage()))
                }
            }
        }
    }

    private fun consumePendingVenue() {
        viewModelScope.launch {
            val selected = sessionRepository.consumePendingVenueSelection(MATCH_VENUE_PICKER_REQUEST_KEY) ?: return@launch
            matchDraftHolder.update { draft ->
                if (draft.kind == MatchType.HAS_VENUE) {
                    draft.copy(venueId = selected.venueId, venueName = selected.venueName, venueAddress = selected.venueAddress)
                } else {
                    val nextVenue = MatchVenue(selected.venueId, selected.venueName, selected.venueAddress)
                    val updatedList = (draft.proposedVenues + nextVenue).distinctBy { it.venueId }.take(MAX_VENUE_OPTIONS)
                    draft.copy(proposedVenues = updatedList)
                }
            }
            sendAction(MatchCreateAction.DraftUpdated(matchDraftHolder.draft.value))
        }
    }

    private fun validateDraft(): Boolean {
        val draft = uiStateFlow.value.draft
        val error =
            when {
                draft.hostTeamId.isBlank() -> "Bạn cần chọn đội đang thao tác để đăng kèo."
                draft.kind == MatchType.HAS_VENUE && draft.venueId.isNullOrBlank() -> "Vui lòng chọn sân bóng."
                draft.kind == MatchType.NEED_VENUE && draft.proposedVenues.isEmpty() -> "Vui lòng chọn ít nhất 1 sân đề xuất."
                draft.matchDate.isBlank() -> "Vui lòng nhập ngày thi đấu (YYYY-MM-DD)."
                else -> null
            }
        if (error != null) {
            sendAction(MatchCreateAction.ValidationError(error))
            return false
        }
        return true
    }

    private fun submitDraft() {
        if (!validateDraft()) return
        val state = uiStateFlow.value
        val draft = state.draft
        sendAction(MatchCreateAction.Submitting(true))
        viewModelScope.launch {
            val result =
                if (state.isEditMode && !draft.matchId.isNullOrBlank()) {
                    updateMatchUseCase(matchId = draft.matchId, draft = draft)
                } else {
                    createMatchUseCase(draft)
                }
            when (result) {
                is Result.Success -> {
                    matchDraftHolder.reset()
                    sendAction(MatchCreateAction.Saved(result.value.matchId))
                }

                is Result.Failure -> {
                    sendAction(MatchCreateAction.ValidationError(result.error.userMessage()))
                }
            }
        }
    }

    private companion object {
        const val MAX_VENUE_OPTIONS = 5
    }
}
