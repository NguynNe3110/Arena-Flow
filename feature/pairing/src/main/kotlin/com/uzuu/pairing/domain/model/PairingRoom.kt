package com.uzuu.pairing.domain.model

data class PairingRoom(
    val pairingId: String,
    val matchId: String,
    val code: String = "#SG-BM-2111",
    val hostTeam: PairingTeam,
    val guestTeam: PairingTeam,
    val version: String = "v1.2",
    val venueOptions: List<MatchVenue>,
    val selectedVenueId: String,
    val timeOptions: List<String>,
    val selectedTimeSlot: String,
    val totalCost: Int = 600000,
    val costSharing: String = "SPLIT_50_50",
    val depositAmount: Int = 200000,
    val depositHolder: String = "Sài Gòn Utd tạm giữ trực tiếp",
    val hostConfirmed: Boolean = true,
    val guestConfirmed: Boolean = false,
    val chatMessages: List<PairingChatMessage> = emptyList(),
)

data class MatchVenue(
    val venueId: String,
    val venueName: String,
    val address: String,
    val venueStatus: String,
)

data class PairingTeam(
    val teamId: String,
    val teamName: String,
    val shortName: String,
    val reliabilityScore: Int,
    val statusLabel: String = "Chưa có sân",
)

data class PairingChatMessage(
    val id: String,
    val senderName: String,
    val message: String,
    val time: String,
    val isMe: Boolean,
)
