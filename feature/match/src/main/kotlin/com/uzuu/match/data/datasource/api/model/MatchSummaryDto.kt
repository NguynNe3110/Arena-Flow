package com.uzuu.match.data.datasource.api.model

import kotlinx.serialization.Serializable

@Serializable
internal data class MatchSummaryDto(
    val matchId: String,
    val type: String,
    val status: String,
    val sportId: String,
    val sportName: String,
    val format: String,
    val skillLevels: List<String>,
    val startsAt: String,
    val endsAt: String,
    val expiresAt: String,
    val authorTeamId: String,
    val authorTeamName: String,
    val authorTeamReliabilityScore: Int,
    val opponentTeamId: String?,
    val opponentTeamName: String?,
    val venueId: String?,
    val venueName: String?,
    val venueAddress: String?,
    val venueOptionCount: Int?,
    val timeOptionCount: Int?,
    val distanceKm: Double?,
    val createdAt: String,
)
