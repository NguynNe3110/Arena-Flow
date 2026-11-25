package com.igorwojda.showcase.feature.pairing.domain.model

/** Lifecycle status of a Match B pairing negotiation room. */
enum class PairingStatus {
    NEGOTIATING,
    CONFIRMED,
    DISPUTED,
    CANCELLED,
    SUPERSEDED,
    EXPIRED,
}

/** Role of the current viewer inside a pairing negotiation room. */
enum class CaptainRole {
    CAPTAIN_A,
    CAPTAIN_B,
    OBSERVER,
}

/** Captain and team readiness state inside a pairing room. */
data class PairingCaptain(
    val teamId: String,
    val teamName: String,
    val shortName: String,
    val captainName: String?,
    val reliabilityScore: Int,
    val hasConfirmed: Boolean,
    val selectedOptionId: String?,
)

/** Proposed venue, schedule, and cost split package option in a pairing room. */
data class PairingOption(
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

/** Input payload for proposing a new venue/time/fee option in a pairing room. */
data class AddPairingOptionInput(
    val venueId: String,
    val venueName: String,
    val venueAddress: String,
    val dateTimeLabel: String,
    val totalCost: Int,
    val feeSplitLabel: String,
    val depositAmount: Int,
    val depositHolder: String,
)

/** Full state of a Match B pairing negotiation room. */
data class PairingRoom(
    val roomId: String,
    val matchId: String,
    val conversationId: String,
    val status: PairingStatus,
    val revision: Int,
    val deadlineAt: String,
    val captainA: PairingCaptain,
    val captainB: PairingCaptain,
    val currentUserRole: CaptainRole,
    val options: List<PairingOption>,
    val selectedOptionId: String?,
    val disputeReason: String? = null,
    val cancelReason: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
)
