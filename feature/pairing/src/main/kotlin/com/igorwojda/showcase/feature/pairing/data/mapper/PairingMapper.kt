package com.igorwojda.showcase.feature.pairing.data.mapper

import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.AddPairingOptionRequestDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.PairingCaptainDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.PairingOptionDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.PairingRoomDto
import com.igorwojda.showcase.feature.pairing.domain.model.AddPairingOptionInput
import com.igorwojda.showcase.feature.pairing.domain.model.CaptainRole
import com.igorwojda.showcase.feature.pairing.domain.model.PairingCaptain
import com.igorwojda.showcase.feature.pairing.domain.model.PairingOption
import com.igorwojda.showcase.feature.pairing.domain.model.PairingRoom
import com.igorwojda.showcase.feature.pairing.domain.model.PairingStatus

internal fun PairingRoomDto.toDomain(): PairingRoom =
    PairingRoom(
        roomId = roomId,
        matchId = matchId,
        conversationId = conversationId,
        status = parseStatus(status),
        revision = revision,
        deadlineAt = deadlineAt,
        captainA = captainA.toDomain(),
        captainB = captainB.toDomain(),
        currentUserRole = parseRole(currentUserRole),
        options = options.map { it.toDomain() },
        selectedOptionId = selectedOptionId,
        disputeReason = disputeReason,
        cancelReason = cancelReason,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

internal fun AddPairingOptionInput.toDto(): AddPairingOptionRequestDto =
    AddPairingOptionRequestDto(
        venueId = venueId,
        venueName = venueName,
        venueAddress = venueAddress,
        dateTimeLabel = dateTimeLabel,
        totalCost = totalCost,
        feeSplitLabel = feeSplitLabel,
        depositAmount = depositAmount,
        depositHolder = depositHolder,
    )

private fun PairingCaptainDto.toDomain(): PairingCaptain =
    PairingCaptain(
        teamId = teamId,
        teamName = teamName,
        shortName = shortName,
        captainName = captainName,
        reliabilityScore = reliabilityScore,
        hasConfirmed = hasConfirmed,
        selectedOptionId = selectedOptionId,
    )

private fun PairingOptionDto.toDomain(): PairingOption =
    PairingOption(
        optionId = optionId,
        venueId = venueId,
        venueName = venueName,
        venueAddress = venueAddress,
        venueStatus = venueStatus,
        dateTimeLabel = dateTimeLabel,
        feeSplitLabel = feeSplitLabel,
        totalCost = totalCost,
        depositAmount = depositAmount,
        depositHolder = depositHolder,
        proposedByTeamId = proposedByTeamId,
        proposedByTeamName = proposedByTeamName,
        isSelectedByA = isSelectedByA,
        isSelectedByB = isSelectedByB,
    )

private fun parseStatus(raw: String): PairingStatus =
    PairingStatus.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: PairingStatus.NEGOTIATING

private fun parseRole(raw: String): CaptainRole =
    CaptainRole.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: CaptainRole.OBSERVER
