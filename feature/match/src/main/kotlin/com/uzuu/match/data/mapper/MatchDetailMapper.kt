package com.uzuu.match.data.mapper

import com.uzuu.match.data.datasource.api.response.MatchDetailDto
import com.uzuu.match.domain.model.MatchCostAgreement
import com.uzuu.match.domain.model.MatchCostItem
import com.uzuu.match.domain.model.MatchDetail
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchTimeSlot
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.domain.model.MatchVenue

internal fun MatchDetailDto.dtoToDomain() = MatchDetail(
    summary = MatchSummary(
        matchId = matchId, type = MatchType.valueOf(type), authorTeamName = authorTeam.teamName,
        venueName = venue?.venueName, skillLevels = skillLevels, status = status, format = format,
        startsAt = startsAt, endsAt = endsAt, expiresAt = expiresAt, authorTeamId = authorTeam.teamId,
        reliabilityScore = authorTeam.reliabilityScore, opponentTeamName = opponentTeam?.teamName,
        venueAddress = venue?.address, venueOptionCount = venueOptions.size, timeOptionCount = timeOptions.size,
    ),
    venueOptions = venueOptions.map { MatchVenue(it.venueId, it.venueName, it.address, it.venueStatus) },
    timeOptions = timeOptions.map { MatchTimeSlot(it.timeOptionId, it.startsAt, it.endsAt) },
    costAgreement = MatchCostAgreement(
        costSplit = costAgreement.costSplit, venueAmount = costAgreement.venueAmount,
        drinksAmount = costAgreement.drinksAmount, depositAmount = costAgreement.depositAmount,
        depositHolder = costAgreement.depositHolder,
        customItems = costAgreement.customItems.map { MatchCostItem(it.itemName, it.amount) },
    ),
    ageNote = ageNote, matchNature = matchNature, cancelReason = cancelReason,
)
