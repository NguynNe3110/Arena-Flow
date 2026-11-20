package com.uzuu.match.data.mapper

import com.uzuu.match.data.datasource.api.model.MatchSummaryDto
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchType

internal fun MatchSummaryDto.dtoToDomain() =
    MatchSummary(
        matchId = matchId,
        type = MatchType.valueOf(type),
        authorTeamName = authorTeamName,
        venueName = venueName,
        skillLevels = skillLevels,
        status = status,
        format = format,
        startsAt = startsAt,
        endsAt = endsAt,
        expiresAt = expiresAt,
        authorTeamId = authorTeamId,
        reliabilityScore = authorTeamReliabilityScore,
        opponentTeamName = opponentTeamName,
        venueAddress = venueAddress,
        venueOptionCount = venueOptionCount ?: 0,
        timeOptionCount = timeOptionCount ?: 0,
        distanceKm = distanceKm,
    )
