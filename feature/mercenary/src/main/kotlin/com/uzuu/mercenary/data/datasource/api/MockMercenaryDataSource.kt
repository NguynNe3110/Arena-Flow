package com.uzuu.mercenary.data.datasource.api

import com.uzuu.mercenary.domain.model.MercenaryPost
import java.util.concurrent.CopyOnWriteArrayList

class MockMercenaryDataSource : MercenaryRemoteDataSource {
    private val mockMercenaryPosts =
        CopyOnWriteArrayList<MercenaryPost>().apply {
            addAll(
                listOf(
                    MercenaryPost(
                        id = "rec-1",
                        teamName = "Bình Minh FC",
                        captainName = "Hải Đăng",
                        reliabilityScore = 92,
                        totalMatches = 34,
                        position = "GK",
                        positionLabel = "Thủ môn",
                        matchDate = "21/11",
                        timeSlot = "19:00–20:30",
                        estimatedArea = "Phường An Khánh, TP. Hồ Chí Minh",
                        distanceKm = 2.5,
                        costShare = "Chia đều tiền sân",
                        requirements = "Ưu tiên bắt gôn sân 7, có găng tay riêng.",
                        openSlots = 1,
                        pendingApplicants = 2,
                        status = "OPEN",
                    ),
                    MercenaryPost(
                        id = "rec-2",
                        teamName = "Sài Gòn United",
                        captainName = "Tuấn Anh",
                        reliabilityScore = 95,
                        totalMatches = 42,
                        position = "DF",
                        positionLabel = "Hậu vệ",
                        matchDate = "21/11",
                        timeSlot = "18:00–19:30",
                        estimatedArea = "Phường Bình Trưng, TP. Hồ Chí Minh",
                        distanceKm = 2.8,
                        costShare = "Chia đều tiền sân",
                        requirements = "Đá phòng ngự chắc chắn, bọc lót tốt.",
                        openSlots = 1,
                        pendingApplicants = 1,
                        status = "OPEN",
                    ),
                    MercenaryPost(
                        id = "rec-3",
                        teamName = "Thảo Điền FC",
                        captainName = "Văn Toàn",
                        reliabilityScore = 90,
                        totalMatches = 29,
                        position = "MF",
                        positionLabel = "Tiền vệ",
                        matchDate = "21/11",
                        timeSlot = "19:00–20:30",
                        estimatedArea = "Phường Thảo Điền, TP. Hồ Chí Minh",
                        distanceKm = 2.1,
                        costShare = "Chia đều tiền sân",
                        requirements = "Thể lực tốt, biết phân phối bóng.",
                        openSlots = 1,
                        pendingApplicants = 3,
                        status = "OPEN",
                    ),
                    MercenaryPost(
                        id = "rec-4",
                        teamName = "An Phú FC",
                        captainName = "Minh Quân",
                        reliabilityScore = 94,
                        totalMatches = 38,
                        position = "FW",
                        positionLabel = "Tiền đạo",
                        matchDate = "21/11",
                        timeSlot = "20:00–21:30",
                        estimatedArea = "Phường An Phú, TP. Hồ Chí Minh",
                        distanceKm = 1.8,
                        costShare = "Chia đều tiền sân",
                        requirements = "Tốc độ tốt, dứt điểm bén.",
                        openSlots = 1,
                        pendingApplicants = 0,
                        status = "OPEN",
                    ),
                    MercenaryPost(
                        id = "rec-5",
                        teamName = "Dầu Khí F.C",
                        captainName = "Hoàng Long",
                        reliabilityScore = 88,
                        totalMatches = 22,
                        position = "GK",
                        positionLabel = "Thủ môn",
                        matchDate = "21/11",
                        timeSlot = "20:30–22:00",
                        estimatedArea = "Phường An Phú, TP. Hồ Chí Minh",
                        distanceKm = 4.5,
                        costShare = "Chia đều tiền sân",
                        requirements = "Bắt gôn chắc tay, giao tiếp tốt.",
                        openSlots = 1,
                        pendingApplicants = 1,
                        status = "OPEN",
                    ),
                ),
            )
        }

    override suspend fun getMercenaryPosts(): List<MercenaryPost> = mockMercenaryPosts.toList()

    override suspend fun applyMercenary(
        postId: String,
        message: String,
    ): MercenaryPost {
        val index = mockMercenaryPosts.indexOfFirst { it.id == postId }
        if (index != -1) {
            val post = mockMercenaryPosts[index]
            val updated =
                post.copy(
                    isApplied = true,
                    pendingApplicants = post.pendingApplicants + 1,
                )
            mockMercenaryPosts[index] = updated
            return updated
        }
        error("Post not found: $postId")
    }
}
