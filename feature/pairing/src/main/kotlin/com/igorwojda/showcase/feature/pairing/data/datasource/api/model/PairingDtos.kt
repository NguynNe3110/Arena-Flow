package com.igorwojda.showcase.feature.pairing.data.datasource.api.model

import kotlinx.serialization.Serializable

/** API DTO representing a captain's state inside a pairing room. */
@Serializable
internal data class PairingCaptainDto(
    val teamId: String,
    val teamName: String,
    val shortName: String,
    val captainName: String? = null,
    val reliabilityScore: Int,
    val hasConfirmed: Boolean,
    val selectedOptionId: String? = null,
)

/** API DTO representing a proposed venue/time/fee option in a pairing room. */
@Serializable
internal data class PairingOptionDto(
    val optionId: String,
    val venueId: String,
    val venueName: String,
    val venueAddress: String,
    val venueStatus: String,
    val dateTimeLabel: String,
    val feeSplitLabel: String,
    val totalCost: Int,
    val depositAmount: Int,
    val depositHolder: String,
    val proposedByTeamId: String,
    val proposedByTeamName: String,
    val isSelectedByA: Boolean,
    val isSelectedByB: Boolean,
)

/** API DTO representing the full pairing negotiation room payload. */
@Serializable
internal data class PairingRoomDto(
    val roomId: String,
    val matchId: String,
    val conversationId: String,
    val status: String,
    val revision: Int,
    val deadlineAt: String,
    val captainA: PairingCaptainDto,
    val captainB: PairingCaptainDto,
    val currentUserRole: String,
    val options: List<PairingOptionDto>,
    val selectedOptionId: String? = null,
    val disputeReason: String? = null,
    val cancelReason: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
)

/** Request body for proposing a new option in a pairing room. */
@Serializable
internal data class AddPairingOptionRequestDto(
    val venueId: String,
    val venueName: String,
    val venueAddress: String,
    val dateTimeLabel: String,
    val totalCost: Int,
    val feeSplitLabel: String,
    val depositAmount: Int,
    val depositHolder: String,
)

/** Request body for confirming a pairing package at a specific revision. */
@Serializable
internal data class ConfirmPairingRequestDto(
    val revision: Int,
    val optionId: String,
)

/** Request body for disputing a pairing room. */
@Serializable
internal data class DisputePairingRequestDto(
    val reason: String,
)

/** Request body for cancelling a pairing room. */
@Serializable
internal data class CancelPairingRequestDto(
    val reason: String,
)
