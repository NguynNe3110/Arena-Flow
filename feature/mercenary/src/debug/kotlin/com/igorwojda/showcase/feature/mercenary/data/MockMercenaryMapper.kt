@file:Suppress("MagicNumber")

package com.igorwojda.showcase.feature.mercenary.data

import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.ArenaStoreSnapshot
import com.igorwojda.showcase.feature.base.data.store.StoreApplication
import com.igorwojda.showcase.feature.base.data.store.StoreRecruitment
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.MercenaryApplicationDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.MercenaryPostDetailDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.MercenaryPostSummaryDto
import kotlin.math.ceil

/** Shared debug mapping and pagination helpers for mock mercenary services. */
internal object MockMercenaryMapper {
    private const val DEFAULT_ALLOWANCE_VND = 100_000
    private const val MIN_PAGE = 1
    private const val MIN_PAGE_SIZE = 1

    fun toSummaryDto(
        store: ArenaInMemoryStore,
        snapshot: ArenaStoreSnapshot,
        recruitment: StoreRecruitment,
    ): MercenaryPostSummaryDto {
        val team = snapshot.teams.firstOrNull { it.id == recruitment.teamId }
        val venue = snapshot.venues.firstOrNull { it.id == recruitment.venueId }
        val match = snapshot.matches.firstOrNull { it.id == recruitment.matchId }
        val extMatch = snapshot.externalMatches.firstOrNull { it.id == recruitment.externalMatchId }
        val currentUserId = snapshot.currentUserId
        val myApp =
            snapshot.applications.firstOrNull {
                it.recruitmentId == recruitment.id && it.applicantId == currentUserId
            }
        val viewerIsCaptain = team?.captainId == currentUserId
        val canSeeCaptain = viewerIsCaptain || myApp?.status == "APPROVED"
        val pendingCount =
            snapshot.applications.count {
                it.recruitmentId == recruitment.id && it.status == "PENDING"
            }
        val format = match?.format ?: venue?.type ?: PitchFormat.SEVEN_A_SIDE
        val startAt = buildStartIso(recruitment.matchDate, recruitment.timeSlot, extMatch?.startTime)
        val endAt = buildEndIso(recruitment.matchDate, recruitment.timeSlot, extMatch?.endTime)

        return MercenaryPostSummaryDto(
            id = recruitment.id,
            teamId = recruitment.teamId,
            teamName = team?.name ?: "Đội bóng",
            captainName = if (canSeeCaptain) store.displayUserName(team?.captainId) else null,
            teamReputationScore = team?.reputationScore ?: 80,
            position = recruitment.position.label,
            neededPositions = listOf(recruitment.position.label),
            slotsNeeded = recruitment.quantity,
            slotsFilled = recruitment.approvedCount,
            pendingApplicationCount = pendingCount,
            status = recruitment.status,
            format = format.name,
            skillLevel = recruitment.skillLevel.label,
            wardId = recruitment.wardId,
            areaLabel = recruitment.area.ifBlank { store.wardLabel(recruitment.wardId) },
            distanceKm = recruitment.distanceKm,
            matchStartAt = startAt,
            matchEndAt = endAt,
            allowanceVnd = DEFAULT_ALLOWANCE_VND,
            notes = recruitment.note,
            myApplicationStatus = myApp?.status,
            myApplicationId = myApp?.id,
            createdAt = recruitment.createdAt,
        )
    }

    fun toDetailDto(
        store: ArenaInMemoryStore,
        snapshot: ArenaStoreSnapshot,
        recruitment: StoreRecruitment,
    ): MercenaryPostDetailDto {
        val summary = toSummaryDto(store, snapshot, recruitment)
        val team = snapshot.teams.firstOrNull { it.id == recruitment.teamId }
        val venue = snapshot.venues.firstOrNull { it.id == recruitment.venueId }
        val currentUserId = snapshot.currentUserId
        val myApp =
            snapshot.applications.firstOrNull {
                it.recruitmentId == recruitment.id && it.applicantId == currentUserId
            }
        val viewerIsCaptain = team?.captainId == currentUserId
        val canSeeVenue = viewerIsCaptain || myApp?.status == "APPROVED"

        return MercenaryPostDetailDto(
            id = summary.id,
            teamId = summary.teamId,
            teamName = summary.teamName,
            teamAvatarUrl = summary.teamAvatarUrl,
            captainName = summary.captainName,
            teamReputationScore = summary.teamReputationScore,
            matchId = recruitment.matchId,
            externalMatchId = recruitment.externalMatchId,
            position = summary.position,
            neededPositions = summary.neededPositions,
            slotsNeeded = summary.slotsNeeded,
            slotsFilled = summary.slotsFilled,
            pendingApplicationCount = summary.pendingApplicationCount,
            status = summary.status,
            format = summary.format,
            skillLevel = summary.skillLevel,
            wardId = summary.wardId,
            areaLabel = summary.areaLabel,
            distanceKm = summary.distanceKm,
            venueId = if (canSeeVenue) venue?.id else null,
            venueName = if (canSeeVenue) venue?.name else null,
            venueAddress = if (canSeeVenue) venue?.address else null,
            matchStartAt = summary.matchStartAt,
            matchEndAt = summary.matchEndAt,
            allowanceVnd = summary.allowanceVnd,
            notes = summary.notes,
            viewerIsCaptain = viewerIsCaptain,
            myApplication = myApp?.let { toApplicationDto(store, snapshot, it, recruitment) },
            createdAt = summary.createdAt,
        )
    }

    fun toApplicationDto(
        store: ArenaInMemoryStore,
        snapshot: ArenaStoreSnapshot,
        application: StoreApplication,
        recruitment: StoreRecruitment? = null,
    ): MercenaryApplicationDto {
        val post = recruitment ?: snapshot.recruitments.firstOrNull { it.id == application.recruitmentId }
        val team = snapshot.teams.firstOrNull { it.id == post?.teamId }
        val applicant = snapshot.users[application.applicantId]
        val posLabel = post?.position?.label ?: "Tiền vệ"
        val startAt =
            post?.let { buildStartIso(it.matchDate, it.timeSlot, null) }
                ?: application.createdAt

        return MercenaryApplicationDto(
            id = application.id,
            postId = application.recruitmentId,
            postTitle = "Tuyển $posLabel · ${team?.name ?: "Đội bóng"}",
            teamName = team?.name ?: "Đội bóng",
            applicantId = application.applicantId,
            applicantName = store.displayUserName(application.applicantId),
            applicantReputationScore = applicant?.reputationScore ?: 80,
            applicantCompletionRate = applicant?.completionRate,
            position = posLabel,
            note = application.message,
            status = application.status,
            conversationId = application.conversationId,
            matchStartAt = startAt,
            areaLabel = post?.area ?: "",
            allowanceVnd = DEFAULT_ALLOWANCE_VND,
            createdAt = application.createdAt,
            approvedAt = application.approvedAt,
        )
    }

    fun <T> paginate(
        items: List<T>,
        page: Int,
        pageSize: Int,
    ): Triple<List<T>, Int, Int> {
        val safeSize = pageSize.coerceAtLeast(MIN_PAGE_SIZE)
        val safePage = page.coerceAtLeast(MIN_PAGE)
        val totalItems = items.size
        val totalPages = ceil(totalItems.toDouble() / safeSize).toInt().coerceAtLeast(1)
        val fromIndex = ((safePage - 1) * safeSize).coerceAtMost(totalItems)
        val toIndex = (fromIndex + safeSize).coerceAtMost(totalItems)
        return Triple(items.subList(fromIndex, toIndex), safePage, totalPages)
    }

    private fun buildStartIso(
        matchDate: String,
        timeSlot: String,
        explicitStart: String?,
    ): String {
        val start = explicitStart ?: timeSlot.substringBefore("-").trim().ifBlank { "19:00" }
        val normalizedTime = if (start.length == 5) "$start:00" else start
        return "${matchDate}T$normalizedTime"
    }

    private fun buildEndIso(
        matchDate: String,
        timeSlot: String,
        explicitEnd: String?,
    ): String {
        val end = explicitEnd ?: timeSlot.substringAfter("-", "").trim().ifBlank { "20:30" }
        val normalizedTime = if (end.length == 5) "$end:00" else end
        return "${matchDate}T$normalizedTime"
    }
}
