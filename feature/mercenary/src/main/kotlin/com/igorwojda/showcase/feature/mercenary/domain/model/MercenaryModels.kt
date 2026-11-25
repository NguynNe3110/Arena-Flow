package com.igorwojda.showcase.feature.mercenary.domain.model

import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.model.SkillLevel

/** Lifecycle status of a mercenary recruitment post. */
enum class MercenaryPostStatus(
    val label: String,
) {
    OPEN("Đang tuyển"),
    FILLED("Đã đủ người"),
    CLOSED("Đã đóng"),
    EXPIRED("Hết hạn"),
    ;

    companion object {
        fun fromApi(value: String?): MercenaryPostStatus = entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: OPEN
    }
}

/** Lifecycle status of a player's mercenary application. */
enum class MercenaryApplicationStatus(
    val label: String,
) {
    PENDING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Từ chối"),
    WITHDRAWN("Đã rút"),
    ;

    companion object {
        fun fromApi(value: String?): MercenaryApplicationStatus =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: PENDING
    }
}

/** Summary model for a mercenary recruitment post in list views. */
data class MercenaryPostSummary(
    val id: String,
    val teamId: String,
    val teamName: String,
    val teamAvatarUrl: String?,
    val captainName: String?,
    val teamReputationScore: Int,
    val position: PlayerPosition,
    val neededPositions: List<PlayerPosition>,
    val slotsNeeded: Int,
    val slotsFilled: Int,
    val pendingApplicationCount: Int,
    val status: MercenaryPostStatus,
    val format: PitchFormat,
    val skillLevel: SkillLevel,
    val wardId: String,
    val areaLabel: String,
    val distanceKm: Double?,
    val matchStartAt: String,
    val matchEndAt: String?,
    val allowanceVnd: Int,
    val notes: String,
    val myApplicationStatus: MercenaryApplicationStatus?,
    val myApplicationId: String?,
    val createdAt: String,
)

/** Detailed model for a single mercenary recruitment post. */
data class MercenaryPostDetail(
    val id: String,
    val teamId: String,
    val teamName: String,
    val teamAvatarUrl: String?,
    val captainName: String?,
    val teamReputationScore: Int,
    val matchId: String?,
    val externalMatchId: String?,
    val position: PlayerPosition,
    val neededPositions: List<PlayerPosition>,
    val slotsNeeded: Int,
    val slotsFilled: Int,
    val pendingApplicationCount: Int,
    val status: MercenaryPostStatus,
    val format: PitchFormat,
    val skillLevel: SkillLevel,
    val wardId: String,
    val areaLabel: String,
    val distanceKm: Double?,
    val venueId: String?,
    val venueName: String?,
    val venueAddress: String?,
    val matchStartAt: String,
    val matchEndAt: String?,
    val allowanceVnd: Int,
    val notes: String,
    val viewerIsCaptain: Boolean,
    val myApplication: MercenaryApplication?,
    val createdAt: String,
)

/** Model for a player's application to a mercenary recruitment post. */
data class MercenaryApplication(
    val id: String,
    val postId: String,
    val postTitle: String,
    val teamName: String,
    val applicantId: String,
    val applicantName: String,
    val applicantReputationScore: Int,
    val applicantCompletionRate: Int?,
    val position: PlayerPosition,
    val note: String,
    val status: MercenaryApplicationStatus,
    val conversationId: String?,
    val matchStartAt: String,
    val areaLabel: String,
    val allowanceVnd: Int,
    val createdAt: String,
    val approvedAt: String?,
)

/** Filter parameters for browsing mercenary posts. */
data class MercenaryPostFilter(
    val wardId: String? = null,
    val position: PlayerPosition? = null,
    val format: PitchFormat? = null,
    val skillLevel: SkillLevel? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
)

/** Paginated list of mercenary post summaries. */
data class MercenaryPage(
    val items: List<MercenaryPostSummary>,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalItems: Int,
)

/** Parameters for creating a new mercenary recruitment post. */
data class CreateMercenaryPostParams(
    val teamId: String,
    val venueId: String,
    val format: PitchFormat,
    val neededPositions: List<PlayerPosition>,
    val slotsNeeded: Int,
    val allowanceVnd: Int,
    val matchStartAt: String,
    val skillLevel: SkillLevel,
    val notes: String,
)

/** Parameters for updating an existing mercenary recruitment post. */
data class UpdateMercenaryPostParams(
    val postId: String,
    val venueId: String,
    val format: PitchFormat,
    val neededPositions: List<PlayerPosition>,
    val slotsNeeded: Int,
    val allowanceVnd: Int,
    val matchStartAt: String,
    val skillLevel: SkillLevel,
    val notes: String,
)
