package com.uzuu.match.domain.model

data class MatchSummary(
    val matchId: String,
    val type: MatchType,
    val authorTeamName: String,
    val venueName: String?,
    val skillLevels: List<String> = emptyList()
)

enum class MatchType {
    HAS_VENUE,
    NEED_VENUE,
}
