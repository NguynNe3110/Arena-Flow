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
                        teamName = "Sài Gòn Utd",
                        captainName = "Tuấn Anh",
                        reliabilityScore = 95,
                        totalMatches = 42,
                        position = "GK",
                        positionLabel = "Thủ môn",
                        matchDate = "T7, 22/11",
                        timeSlot = "19:30 - 21:00",
                        estimatedArea = "Bình Thạnh, TP.HCM",
                        distanceKm = 2.4,
                        costShare = "Miễn phí (Team bao)",
                        requirements = "Ưu tiên bắt gôn sân 7, có găng tay riêng, bay nhảy ổn định.",
                        openSlots = 1,
                        pendingApplicants = 2,
                        status = "OPEN",
                    ),
                    MercenaryPost(
                        id = "rec-2",
                        teamName = "Bình Minh FC",
                        captainName = "Hải Đăng",
                        reliabilityScore = 98,
                        totalMatches = 58,
                        position = "DF",
                        positionLabel = "Hậu vệ thòng",
                        matchDate = "CN, 23/11",
                        timeSlot = "20:00 - 21:30",
                        estimatedArea = "Thủ Đức, TP.HCM",
                        distanceKm = 4.1,
                        costShare = "Chia đều tiền sân (~50k)",
                        requirements = "Đá phòng ngự chắc chắn, bọc lót tốt, không chơi thô bạo.",
                        openSlots = 2,
                        pendingApplicants = 1,
                        status = "OPEN",
                    ),
                    MercenaryPost(
                        id = "rec-3",
                        teamName = "Dầu Khí F.C",
                        captainName = "Văn Toàn",
                        reliabilityScore = 90,
                        totalMatches = 29,
                        position = "MF",
                        positionLabel = "Tiền vệ trung tâm",
                        matchDate = "T2, 24/11",
                        timeSlot = "18:30 - 20:00",
                        estimatedArea = "Quận 7, TP.HCM",
                        distanceKm = 6.8,
                        costShare = "Nước + sân tài trợ 50%",
                        requirements = "Thể lực tốt, biết phân phối bóng và hỗ trợ lùi về.",
                        openSlots = 1,
                        pendingApplicants = 3,
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
