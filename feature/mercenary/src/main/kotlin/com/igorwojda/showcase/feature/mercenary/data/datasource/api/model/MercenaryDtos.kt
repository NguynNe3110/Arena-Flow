package com.igorwojda.showcase.feature.mercenary.data.datasource.api.model

import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.model.SkillLevel
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryApplication
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryApplicationStatus
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostDetail
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostStatus
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostSummary
import kotlinx.serialization.Serializable

/** Network DTO for a mercenary post summary item. */
@Serializable
data class MercenaryPostSummaryDto(
    val id: String,
    val teamId: String,
    val teamName: String,
    val teamAvatarUrl: String? = null,
    val captainName: String? = null,
    val teamReputationScore: Int = 80,
    val position: String,
    val neededPositions: List<String> = emptyList(),
    val slotsNeeded: Int,
    val slotsFilled: Int = 0,
    val pendingApplicationCount: Int = 0,
    val status: String,
    val format: String = "SEVEN_A_SIDE",
    val skillLevel: String = "INTERMEDIATE",
    val wardId: String = "",
    val areaLabel: String = "",
    val distanceKm: Double? = null,
    val matchStartAt: String,
    val matchEndAt: String? = null,
    val allowanceVnd: Int = 0,
    val notes: String = "",
    val myApplicationStatus: String? = null,
    val myApplicationId: String? = null,
    val createdAt: String = "",
)

/** Network DTO for a mercenary post detail response. */
@Serializable
data class MercenaryPostDetailDto(
    val id: String,
    val teamId: String,
    val teamName: String,
    val teamAvatarUrl: String? = null,
    val captainName: String? = null,
    val teamReputationScore: Int = 80,
    val matchId: String? = null,
    val externalMatchId: String? = null,
    val position: String,
    val neededPositions: List<String> = emptyList(),
    val slotsNeeded: Int,
    val slotsFilled: Int = 0,
    val pendingApplicationCount: Int = 0,
    val status: String,
    val format: String = "SEVEN_A_SIDE",
    val skillLevel: String = "INTERMEDIATE",
    val wardId: String = "",
    val areaLabel: String = "",
    val distanceKm: Double? = null,
    val venueId: String? = null,
    val venueName: String? = null,
    val venueAddress: String? = null,
    val matchStartAt: String,
    val matchEndAt: String? = null,
    val allowanceVnd: Int = 0,
    val notes: String = "",
    val viewerIsCaptain: Boolean = false,
    val myApplication: MercenaryApplicationDto? = null,
    val createdAt: String = "",
)

/** Network DTO for a mercenary application item. */
@Serializable
data class MercenaryApplicationDto(
    val id: String,
    val postId: String,
    val postTitle: String = "",
    val teamName: String = "",
    val applicantId: String,
    val applicantName: String = "",
    val applicantReputationScore: Int = 80,
    val applicantCompletionRate: Int? = null,
    val position: String,
    val note: String = "",
    val status: String,
    val conversationId: String? = null,
    val matchStartAt: String = "",
    val areaLabel: String = "",
    val allowanceVnd: Int = 0,
    val createdAt: String = "",
    val approvedAt: String? = null,
)

/** Request DTO for creating a mercenary recruitment post. */
@Serializable
data class CreateMercenaryPostRequestDto(
    val teamId: String,
    val venueId: String,
    val format: String,
    val neededPositions: List<String>,
    val slotsNeeded: Int,
    val allowanceVnd: Int,
    val matchStartAt: String,
    val skillLevel: String,
    val notes: String,
)

/** Request DTO for updating a mercenary recruitment post. */
@Serializable
data class UpdateMercenaryPostRequestDto(
    val venueId: String,
    val format: String,
    val neededPositions: List<String>,
    val slotsNeeded: Int,
    val allowanceVnd: Int,
    val matchStartAt: String,
    val skillLevel: String,
    val notes: String,
)

/** Request DTO for applying to a mercenary recruitment post. */
@Serializable
data class ApplyMercenaryRequestDto(
    val position: String,
    val note: String,
)

internal fun MercenaryPostSummaryDto.toDomain(): MercenaryPostSummary {
    val primaryPos = PlayerPosition.fromApiLabel(position) ?: PlayerPosition.MIDFIELDER
    val positions =
        neededPositions
            .mapNotNull { PlayerPosition.fromApiLabel(it) }
            .ifEmpty { listOf(primaryPos) }
    return MercenaryPostSummary(
        id = id,
        teamId = teamId,
        teamName = teamName,
        teamAvatarUrl = teamAvatarUrl,
        captainName = captainName,
        teamReputationScore = teamReputationScore,
        position = primaryPos,
        neededPositions = positions,
        slotsNeeded = slotsNeeded,
        slotsFilled = slotsFilled,
        pendingApplicationCount = pendingApplicationCount,
        status = MercenaryPostStatus.fromApi(status),
        format = PitchFormat.fromApiName(format) ?: PitchFormat.SEVEN_A_SIDE,
        skillLevel = SkillLevel.fromApiLabel(skillLevel) ?: SkillLevel.INTERMEDIATE,
        wardId = wardId,
        areaLabel = areaLabel,
        distanceKm = distanceKm,
        matchStartAt = matchStartAt,
        matchEndAt = matchEndAt,
        allowanceVnd = allowanceVnd,
        notes = notes,
        myApplicationStatus = myApplicationStatus?.let { MercenaryApplicationStatus.fromApi(it) },
        myApplicationId = myApplicationId,
        createdAt = createdAt,
    )
}

internal fun MercenaryPostDetailDto.toDomain(): MercenaryPostDetail {
    val primaryPos = PlayerPosition.fromApiLabel(position) ?: PlayerPosition.MIDFIELDER
    val positions =
        neededPositions
            .mapNotNull { PlayerPosition.fromApiLabel(it) }
            .ifEmpty { listOf(primaryPos) }
    return MercenaryPostDetail(
        id = id,
        teamId = teamId,
        teamName = teamName,
        teamAvatarUrl = teamAvatarUrl,
        captainName = captainName,
        teamReputationScore = teamReputationScore,
        matchId = matchId,
        externalMatchId = externalMatchId,
        position = primaryPos,
        neededPositions = positions,
        slotsNeeded = slotsNeeded,
        slotsFilled = slotsFilled,
        pendingApplicationCount = pendingApplicationCount,
        status = MercenaryPostStatus.fromApi(status),
        format = PitchFormat.fromApiName(format) ?: PitchFormat.SEVEN_A_SIDE,
        skillLevel = SkillLevel.fromApiLabel(skillLevel) ?: SkillLevel.INTERMEDIATE,
        wardId = wardId,
        areaLabel = areaLabel,
        distanceKm = distanceKm,
        venueId = venueId,
        venueName = venueName,
        venueAddress = venueAddress,
        matchStartAt = matchStartAt,
        matchEndAt = matchEndAt,
        allowanceVnd = allowanceVnd,
        notes = notes,
        viewerIsCaptain = viewerIsCaptain,
        myApplication = myApplication?.toDomain(),
        createdAt = createdAt,
    )
}

internal fun MercenaryApplicationDto.toDomain(): MercenaryApplication =
    MercenaryApplication(
        id = id,
        postId = postId,
        postTitle = postTitle,
        teamName = teamName,
        applicantId = applicantId,
        applicantName = applicantName,
        applicantReputationScore = applicantReputationScore,
        applicantCompletionRate = applicantCompletionRate,
        position = PlayerPosition.fromApiLabel(position) ?: PlayerPosition.MIDFIELDER,
        note = note,
        status = MercenaryApplicationStatus.fromApi(status),
        conversationId = conversationId,
        matchStartAt = matchStartAt,
        areaLabel = areaLabel,
        allowanceVnd = allowanceVnd,
        createdAt = createdAt,
        approvedAt = approvedAt,
    )
