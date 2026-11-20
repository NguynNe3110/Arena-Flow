package com.uzuu.match.domain.model

data class MatchSummary(
    val matchId: String,
    val type: MatchType,
    val authorTeamName: String,
    val venueName: String?,
    val skillLevels: List<String> = emptyList(),
    val status: String = "OPEN",
    val format: String = "SEVEN_A_SIDE",
    val startsAt: String = "",
    val endsAt: String = "",
    val expiresAt: String = "",
    val authorTeamId: String = "",
    val reliabilityScore: Int = 100,
    val opponentTeamName: String? = null,
    val venueAddress: String? = null,
    val venueOptionCount: Int = 0,
    val timeOptionCount: Int = 0,
    val distanceKm: Double? = null,
)

enum class MatchType {
    HAS_VENUE,
    NEED_VENUE,
}
