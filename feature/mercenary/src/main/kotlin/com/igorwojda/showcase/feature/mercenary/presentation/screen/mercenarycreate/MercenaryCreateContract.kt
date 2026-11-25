package com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarycreate

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.model.SkillLevel
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState

/** UI state for [MercenaryCreateScreen]. */
internal data class MercenaryCreateUiState(
    val editingPostId: String? = null,
    val teamId: String = "team-alpha",
    val venueId: String = "",
    val venueName: String = "",
    val venueAddress: String = "",
    val format: PitchFormat = PitchFormat.SEVEN_A_SIDE,
    val neededPositions: Set<PlayerPosition> = setOf(PlayerPosition.MIDFIELDER),
    val slotsNeeded: Int = 1,
    val allowanceVndText: String = "100000",
    val matchStartAt: String = "2026-11-23T19:00:00",
    val skillLevel: SkillLevel = SkillLevel.INTERMEDIATE,
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val savedPostId: String? = null,
    val error: AppError? = null,
    val validationMessage: String? = null,
) : BaseState

/** User events dispatched from [MercenaryCreateScreen] to [MercenaryCreateViewModel]. */
internal sealed interface MercenaryCreateEvent {
    data class Initialize(
        val postId: String?,
    ) : MercenaryCreateEvent

    data class FormatChanged(
        val format: PitchFormat,
    ) : MercenaryCreateEvent

    data class TogglePosition(
        val position: PlayerPosition,
    ) : MercenaryCreateEvent

    data class SlotsChanged(
        val slots: Int,
    ) : MercenaryCreateEvent

    data class AllowanceChanged(
        val allowanceText: String,
    ) : MercenaryCreateEvent

    data class MatchStartAtChanged(
        val matchStartAt: String,
    ) : MercenaryCreateEvent

    data class SkillLevelChanged(
        val skillLevel: SkillLevel,
    ) : MercenaryCreateEvent

    data class NotesChanged(
        val notes: String,
    ) : MercenaryCreateEvent

    data object Submit : MercenaryCreateEvent

    data object ClearValidation : MercenaryCreateEvent
}

/** State reducers for [MercenaryCreateViewModel]. */
internal sealed interface MercenaryCreateAction : BaseAction<MercenaryCreateUiState> {
    data class SetLoading(
        val isLoading: Boolean,
    ) : MercenaryCreateAction {
        override fun reduce(state: MercenaryCreateUiState) = state.copy(isLoading = isLoading, error = null)
    }

    data class SetTeamId(
        val teamId: String,
    ) : MercenaryCreateAction {
        override fun reduce(state: MercenaryCreateUiState) = state.copy(teamId = teamId)
    }

    data class SetVenue(
        val venueId: String,
        val venueName: String,
        val venueAddress: String,
    ) : MercenaryCreateAction {
        override fun reduce(state: MercenaryCreateUiState) =
            state.copy(venueId = venueId, venueName = venueName, venueAddress = venueAddress)
    }

    data class PopulateFromPost(
        val postId: String,
        val teamId: String,
        val venueId: String,
        val venueName: String,
        val venueAddress: String,
        val format: PitchFormat,
        val positions: Set<PlayerPosition>,
        val slotsNeeded: Int,
        val allowanceVnd: Int,
        val matchStartAt: String,
        val skillLevel: SkillLevel,
        val notes: String,
    ) : MercenaryCreateAction {
        override fun reduce(state: MercenaryCreateUiState) =
            state.copy(
                editingPostId = postId,
                teamId = teamId,
                venueId = venueId,
                venueName = venueName,
                venueAddress = venueAddress,
                format = format,
                neededPositions = positions,
                slotsNeeded = slotsNeeded,
                allowanceVndText = allowanceVnd.toString(),
                matchStartAt = matchStartAt,
                skillLevel = skillLevel,
                notes = notes,
                isLoading = false,
            )
    }

    data class SetFormat(
        val format: PitchFormat,
    ) : MercenaryCreateAction {
        override fun reduce(state: MercenaryCreateUiState) = state.copy(format = format)
    }

    data class SetPositions(
        val positions: Set<PlayerPosition>,
    ) : MercenaryCreateAction {
        override fun reduce(state: MercenaryCreateUiState) = state.copy(neededPositions = positions)
    }

    data class SetSlots(
        val slots: Int,
    ) : MercenaryCreateAction {
        override fun reduce(state: MercenaryCreateUiState) = state.copy(slotsNeeded = slots.coerceIn(1, MAX_SLOTS))
    }

    data class SetAllowanceText(
        val text: String,
    ) : MercenaryCreateAction {
        override fun reduce(state: MercenaryCreateUiState) = state.copy(allowanceVndText = text)
    }

    data class SetMatchStartAt(
        val value: String,
    ) : MercenaryCreateAction {
        override fun reduce(state: MercenaryCreateUiState) = state.copy(matchStartAt = value)
    }

    data class SetSkillLevel(
        val skillLevel: SkillLevel,
    ) : MercenaryCreateAction {
        override fun reduce(state: MercenaryCreateUiState) = state.copy(skillLevel = skillLevel)
    }

    data class SetNotes(
        val notes: String,
    ) : MercenaryCreateAction {
        override fun reduce(state: MercenaryCreateUiState) = state.copy(notes = notes)
    }

    data class SetSubmitting(
        val isSubmitting: Boolean,
    ) : MercenaryCreateAction {
        override fun reduce(state: MercenaryCreateUiState) = state.copy(isSubmitting = isSubmitting)
    }

    data class Saved(
        val postId: String,
    ) : MercenaryCreateAction {
        override fun reduce(state: MercenaryCreateUiState) = state.copy(isSubmitting = false, savedPostId = postId)
    }

    data class SetValidation(
        val message: String?,
    ) : MercenaryCreateAction {
        override fun reduce(state: MercenaryCreateUiState) = state.copy(validationMessage = message)
    }

    data class SetError(
        val error: AppError,
    ) : MercenaryCreateAction {
        override fun reduce(state: MercenaryCreateUiState) = state.copy(isLoading = false, isSubmitting = false, error = error)
    }

    private companion object {
        const val MAX_SLOTS = 10
    }
}
