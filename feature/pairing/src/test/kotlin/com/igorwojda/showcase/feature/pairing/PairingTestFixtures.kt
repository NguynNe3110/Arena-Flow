package com.igorwojda.showcase.feature.pairing

import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.PairingCaptainDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.PairingOptionDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.PairingRoomDto
import com.igorwojda.showcase.feature.pairing.domain.model.AddPairingOptionInput
import com.igorwojda.showcase.feature.pairing.domain.model.CaptainRole
import com.igorwojda.showcase.feature.pairing.domain.model.PairingCaptain
import com.igorwojda.showcase.feature.pairing.domain.model.PairingOption
import com.igorwojda.showcase.feature.pairing.domain.model.PairingRoom
import com.igorwojda.showcase.feature.pairing.domain.model.PairingStatus

internal object PairingTestFixtures {
    fun sampleRoom(
        status: PairingStatus = PairingStatus.NEGOTIATING,
        revision: Int = 1,
        hostConfirmed: Boolean = false,
        guestConfirmed: Boolean = false,
        otherSelected: Boolean = false,
    ): PairingRoom =
        PairingRoom(
            roomId = "pairing-beta-alpha",
            matchId = "match-b-open",
            conversationId = "conv-pairing-alpha",
            status = status,
            revision = revision,
            deadlineAt = "2026-11-18T18:15:00",
            captainA =
                PairingCaptain(
                    teamId = "team-beta",
                    teamName = "Sài Gòn United",
                    shortName = "SGU",
                    captainName = "Phạm Văn Hùng",
                    reliabilityScore = 88,
                    hasConfirmed = hostConfirmed,
                    selectedOptionId = if (otherSelected) "opt-1" else null,
                ),
            captainB =
                PairingCaptain(
                    teamId = "team-alpha",
                    teamName = "FC Sao Mai",
                    shortName = "SM",
                    captainName = "Nguyễn Văn A",
                    reliabilityScore = 92,
                    hasConfirmed = guestConfirmed,
                    selectedOptionId = "opt-1",
                ),
            currentUserRole = CaptainRole.CAPTAIN_B,
            options = listOf(sampleOption()),
            selectedOptionId = "opt-1",
            createdAt = "2026-11-18T16:15:00",
            updatedAt = "2026-11-18T16:20:00",
        )

    fun sampleOption(optionId: String = "opt-1"): PairingOption =
        PairingOption(
            optionId = optionId,
            venueId = "ven-anphu",
            venueName = "Sân bóng An Phú",
            venueAddress = "Số 8 Đường số 2, TP. Thủ Đức",
            venueStatus = "ACTIVE",
            dateTimeLabel = "2026-11-21 · 19:30 - 21:00",
            feeSplitLabel = "Chia đôi 50-50",
            totalCost = 600_000,
            depositAmount = 200_000,
            depositHolder = "Đội chủ nhà",
            proposedByTeamId = "team-beta",
            proposedByTeamName = "Sài Gòn United",
            isSelectedByA = false,
            isSelectedByB = true,
        )

    fun sampleAddOptionInput(): AddPairingOptionInput =
        AddPairingOptionInput(
            venueId = "ven-binhtrung",
            venueName = "Sân bóng Bình Trưng",
            venueAddress = "12 Đỗ Xuân Hợp, TP. Thủ Đức",
            dateTimeLabel = "2026-11-22 · 18:00 - 19:30",
            totalCost = 500_000,
            feeSplitLabel = "Chia đôi 50-50",
            depositAmount = 100_000,
            depositHolder = "Đội chủ nhà",
        )

    fun sampleRoomDto(status: String = "NEGOTIATING"): PairingRoomDto =
        PairingRoomDto(
            roomId = "pairing-beta-alpha",
            matchId = "match-b-open",
            conversationId = "conv-pairing-alpha",
            status = status,
            revision = 1,
            deadlineAt = "2026-11-18T18:15:00",
            captainA =
                PairingCaptainDto(
                    teamId = "team-beta",
                    teamName = "Sài Gòn United",
                    shortName = "SGU",
                    captainName = "Phạm Văn Hùng",
                    reliabilityScore = 88,
                    hasConfirmed = false,
                    selectedOptionId = null,
                ),
            captainB =
                PairingCaptainDto(
                    teamId = "team-alpha",
                    teamName = "FC Sao Mai",
                    shortName = "SM",
                    captainName = "Nguyễn Văn A",
                    reliabilityScore = 92,
                    hasConfirmed = false,
                    selectedOptionId = "opt-1",
                ),
            currentUserRole = "CAPTAIN_B",
            options =
                listOf(
                    PairingOptionDto(
                        optionId = "opt-1",
                        venueId = "ven-anphu",
                        venueName = "Sân bóng An Phú",
                        venueAddress = "Số 8 Đường số 2, TP. Thủ Đức",
                        venueStatus = "ACTIVE",
                        dateTimeLabel = "2026-11-21 · 19:30 - 21:00",
                        feeSplitLabel = "Chia đôi 50-50",
                        totalCost = 600_000,
                        depositAmount = 200_000,
                        depositHolder = "Đội chủ nhà",
                        proposedByTeamId = "team-beta",
                        proposedByTeamName = "Sài Gòn United",
                        isSelectedByA = false,
                        isSelectedByB = true,
                    ),
                ),
            selectedOptionId = "opt-1",
            createdAt = "2026-11-18T16:15:00",
            updatedAt = "2026-11-18T16:20:00",
        )
}
