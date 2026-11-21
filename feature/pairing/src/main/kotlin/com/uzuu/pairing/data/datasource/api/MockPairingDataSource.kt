package com.uzuu.pairing.data.datasource.api

import com.uzuu.pairing.domain.model.MatchVenue
import com.uzuu.pairing.domain.model.PairingChatMessage
import com.uzuu.pairing.domain.model.PairingRoom
import com.uzuu.pairing.domain.model.PairingTeam

class MockPairingDataSource : PairingRemoteDataSource {
    private var mockPairingRoom =
        PairingRoom(
            pairingId = "pair-101",
            matchId = "match-b-negotiable",
            code = "#SG-BM-2111",
            hostTeam = PairingTeam("team-beta", "Sài Gòn Utd", "SG", 95, "Chưa có sân"),
            guestTeam = PairingTeam("team-alpha", "Bình Minh FC", "BM", 98, "Đang đàm phán"),
            version = "v1.2",
            venueOptions =
                listOf(
                    MatchVenue(
                        venueId = "ven-binhthanh",
                        venueName = "Sân bóng Bình Thạnh Sport",
                        address = "Hẻm 48 Điện Biên Phủ, P.25, Bình Thạnh",
                        venueStatus = "ACTIVE",
                    ),
                    MatchVenue(
                        venueId = "ven-anphu",
                        venueName = "Sân bóng An Phú",
                        address = "Đường số 12, P. An Khánh, TP. Thủ Đức",
                        venueStatus = "ACTIVE",
                    ),
                ),
            selectedVenueId = "ven-binhthanh",
            timeOptions = listOf("19:30 - 21:00", "20:30 - 22:00"),
            selectedTimeSlot = "19:30 - 21:00",
            totalCost = 600000,
            costSharing = "SPLIT_50_50",
            depositAmount = 200000,
            depositHolder = "Sài Gòn Utd tạm giữ trực tiếp",
            hostConfirmed = true,
            guestConfirmed = false,
            chatMessages =
                listOf(
                    PairingChatMessage(
                        id = "1",
                        senderName = "Sài Gòn Utd",
                        message = "Chào Bình Minh FC! Đội mình muốn đá sân Bình Thạnh khung 19:30 nhé.",
                        time = "10:15",
                        isMe = false,
                    ),
                    PairingChatMessage(
                        id = "2",
                        senderName = "Bình Minh FC",
                        message = "Chào bạn, khung giờ này đội mình đá được. Đã kiểm tra chi phí.",
                        time = "10:18",
                        isMe = true,
                    ),
                ),
        )

    override suspend fun getPairingRoom(pairingId: String): PairingRoom = mockPairingRoom

    override suspend fun updatePackageOption(
        pairingId: String,
        venueId: String?,
        timeSlot: String?,
    ): PairingRoom {
        mockPairingRoom =
            mockPairingRoom.copy(
                selectedVenueId = venueId ?: mockPairingRoom.selectedVenueId,
                selectedTimeSlot = timeSlot ?: mockPairingRoom.selectedTimeSlot,
                hostConfirmed = true,
                guestConfirmed = false,
                version = "v1." + ((mockPairingRoom.version.removePrefix("v1.").toIntOrNull() ?: 2) + 1),
            )
        return mockPairingRoom
    }

    override suspend fun confirmPackage(pairingId: String): PairingRoom {
        mockPairingRoom =
            mockPairingRoom.copy(
                guestConfirmed = true,
                hostConfirmed = true,
            )
        return mockPairingRoom
    }
}
