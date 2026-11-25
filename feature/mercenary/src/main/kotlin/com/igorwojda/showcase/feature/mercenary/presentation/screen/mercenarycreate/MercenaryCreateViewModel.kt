package com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarycreate

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.mercenary.domain.model.CreateMercenaryPostParams
import com.igorwojda.showcase.feature.mercenary.domain.model.UpdateMercenaryPostParams
import com.igorwojda.showcase.feature.mercenary.domain.usecase.CreateMercenaryPostUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.GetMercenaryPostDetailUseCase
import com.igorwojda.showcase.feature.mercenary.domain.usecase.UpdateMercenaryPostUseCase
import kotlinx.coroutines.launch

/** ViewModel managing creation and editing of mercenary recruitment posts. */
internal class MercenaryCreateViewModel(
    private val createMercenaryPostUseCase: CreateMercenaryPostUseCase,
    private val getMercenaryPostDetailUseCase: GetMercenaryPostDetailUseCase,
    private val sessionRepository: SessionRepository,
    private val updateMercenaryPostUseCase: UpdateMercenaryPostUseCase,
) : BaseViewModel<MercenaryCreateUiState, MercenaryCreateAction>(MercenaryCreateUiState()) {
    init {
        observeSession()
    }

    /** Handles all user events from [MercenaryCreateScreen]. */
    fun onEvent(event: MercenaryCreateEvent) {
        when (event) {
            is MercenaryCreateEvent.Initialize -> {
                initialize(event.postId)
            }

            is MercenaryCreateEvent.FormatChanged -> {
                sendAction(MercenaryCreateAction.SetFormat(event.format))
            }

            is MercenaryCreateEvent.TogglePosition -> {
                togglePosition(event.position)
            }

            is MercenaryCreateEvent.SlotsChanged -> {
                sendAction(MercenaryCreateAction.SetSlots(event.slots))
            }

            is MercenaryCreateEvent.AllowanceChanged -> {
                sendAction(MercenaryCreateAction.SetAllowanceText(event.allowanceText))
            }

            is MercenaryCreateEvent.MatchStartAtChanged -> {
                sendAction(MercenaryCreateAction.SetMatchStartAt(event.matchStartAt))
            }

            is MercenaryCreateEvent.SkillLevelChanged -> {
                sendAction(MercenaryCreateAction.SetSkillLevel(event.skillLevel))
            }

            is MercenaryCreateEvent.NotesChanged -> {
                sendAction(MercenaryCreateAction.SetNotes(event.notes))
            }

            MercenaryCreateEvent.Submit -> {
                submit()
            }

            MercenaryCreateEvent.ClearValidation -> {
                sendAction(MercenaryCreateAction.SetValidation(null))
            }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionRepository.observeSession().collect { session ->
                session.actingTeam?.id?.let { teamId ->
                    sendAction(MercenaryCreateAction.SetTeamId(teamId))
                }
                val pending = session.pendingVenueSelection
                if (pending != null && pending.requestKey == VENUE_PICKER_KEY) {
                    sessionRepository.consumePendingVenueSelection(VENUE_PICKER_KEY)
                    sendAction(
                        MercenaryCreateAction.SetVenue(
                            venueId = pending.venueId,
                            venueName = pending.venueName,
                            venueAddress = pending.venueAddress,
                        ),
                    )
                }
            }
        }
    }

    private fun initialize(postId: String?) {
        if (postId.isNullOrBlank()) return
        viewModelScope.launch {
            sendAction(MercenaryCreateAction.SetLoading(true))
            when (val result = getMercenaryPostDetailUseCase(postId)) {
                is Result.Success -> {
                    val post = result.value
                    sendAction(
                        MercenaryCreateAction.PopulateFromPost(
                            postId = post.id,
                            teamId = post.teamId,
                            venueId = post.venueId.orEmpty(),
                            venueName = post.venueName.orEmpty(),
                            venueAddress = post.venueAddress ?: post.areaLabel,
                            format = post.format,
                            positions = post.neededPositions.toSet(),
                            slotsNeeded = post.slotsNeeded,
                            allowanceVnd = post.allowanceVnd,
                            matchStartAt = post.matchStartAt,
                            skillLevel = post.skillLevel,
                            notes = post.notes,
                        ),
                    )
                }

                is Result.Failure -> {
                    sendAction(MercenaryCreateAction.SetError(result.error))
                }
            }
        }
    }

    private fun togglePosition(position: PlayerPosition) {
        val current = uiStateFlow.value.neededPositions
        val next =
            if (position in current && current.size > 1) {
                current - position
            } else {
                current + position
            }
        sendAction(MercenaryCreateAction.SetPositions(next))
    }

    private fun submit() {
        val state = uiStateFlow.value
        if (state.isSubmitting) return
        if (state.venueId.isBlank()) {
            sendAction(MercenaryCreateAction.SetValidation("Vui lòng chọn sân thi đấu."))
            return
        }
        val allowance = state.allowanceVndText.toIntOrNull() ?: 0
        viewModelScope.launch {
            sendAction(MercenaryCreateAction.SetSubmitting(true))
            val editingId = state.editingPostId
            val result =
                if (editingId == null) {
                    createMercenaryPostUseCase(
                        CreateMercenaryPostParams(
                            teamId = state.teamId,
                            venueId = state.venueId,
                            format = state.format,
                            neededPositions = state.neededPositions.toList(),
                            slotsNeeded = state.slotsNeeded,
                            allowanceVnd = allowance,
                            matchStartAt = state.matchStartAt,
                            skillLevel = state.skillLevel,
                            notes = state.notes,
                        ),
                    )
                } else {
                    updateMercenaryPostUseCase(
                        UpdateMercenaryPostParams(
                            postId = editingId,
                            venueId = state.venueId,
                            format = state.format,
                            neededPositions = state.neededPositions.toList(),
                            slotsNeeded = state.slotsNeeded,
                            allowanceVnd = allowance,
                            matchStartAt = state.matchStartAt,
                            skillLevel = state.skillLevel,
                            notes = state.notes,
                        ),
                    )
                }
            when (result) {
                is Result.Success -> {
                    sendAction(MercenaryCreateAction.Saved(result.value.id))
                }

                is Result.Failure -> {
                    sendAction(MercenaryCreateAction.SetSubmitting(false))
                    sendAction(MercenaryCreateAction.SetValidation(result.error.userMessage()))
                }
            }
        }
    }

    internal companion object {
        const val VENUE_PICKER_KEY = "mercenary_create_venue"
    }
}
