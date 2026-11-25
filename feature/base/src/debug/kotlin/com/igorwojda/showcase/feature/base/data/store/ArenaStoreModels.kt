@file:Suppress("TooManyFunctions", "LongParameterList")

package com.igorwojda.showcase.feature.base.data.store

import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.GpsPermissionState
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.model.SelectedVenueResult
import com.igorwojda.showcase.feature.base.domain.model.SimulatedError
import com.igorwojda.showcase.feature.base.domain.model.SkillLevel
import com.igorwojda.showcase.feature.base.domain.model.WardOption

/** In-memory store record for an ArenaFlow user (Debug only). */
data class StoreUser(
    val id: String,
    val phone: String,
    val email: String? = null,
    val displayName: String,
    val realName: String? = null,
    val preferredPosition: PlayerPosition? = null,
    val wardId: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val bio: String = "",
    val reputationScore: Int = 100,
    val completionRate: Int? = null,
    val matchCount: Int = 0,
    val isNewUser: Boolean = false,
    val isDeleted: Boolean = false,
    val role: AccountRole = AccountRole.USER,
)

/** In-memory store record for a football team (Debug only). */
data class StoreTeam(
    val id: String,
    val name: String,
    val shortName: String,
    val description: String,
    val captainId: String,
    val wardId: String,
    val status: String,
    val skillLevels: List<SkillLevel>,
    val reputationScore: Int,
    val ratingAvg: Double,
    val completionRate: Int?,
    val matchCount: Int,
    val createdAt: String,
    val disbandReason: String? = null,
    val disbandedAt: String? = null,
    val inactiveReason: String? = null,
)

/** In-memory store record for a team member (Debug only). */
data class StoreTeamMember(
    val id: String,
    val teamId: String,
    val userId: String,
    val role: String,
    val joinedAt: String,
    val status: String,
    val leftAt: String? = null,
)

/** In-memory store record for a captaincy transfer event (Debug only). */
data class StoreCaptainHistory(
    val id: String,
    val teamId: String,
    val previousCaptainId: String?,
    val newCaptainId: String,
    val changedAt: String,
    val note: String,
)

/** In-memory store record for a team invitation (Debug only). */
data class StoreInvitation(
    val id: String,
    val teamId: String,
    val invitedUserId: String,
    val invitedBy: String,
    val status: String,
    val message: String,
    val createdAt: String,
)

/** In-memory store record for a venue photo (Debug only). */
data class StoreVenuePhoto(
    val id: String,
    val venueId: String,
    val label: String,
    val imageUrl: String,
    val isPrimary: Boolean,
    val displayOrder: Int,
)

/** In-memory store record for a football venue (Debug only). */
data class StoreVenue(
    val id: String,
    val name: String,
    val address: String,
    val wardId: String,
    val lat: Double,
    val lng: Double,
    val distanceKm: Double,
    val type: PitchFormat,
    val contactPhone: String?,
    val website: String? = null,
    val status: String,
    val createdBy: String,
    val warning: String? = null,
    val reportCount: Int = 0,
)

/** In-memory store record for a Match A or Match B post (Debug only). */
data class StoreMatch(
    val id: String,
    val kind: String,
    val hostTeamId: String,
    val guestTeamId: String? = null,
    val format: PitchFormat,
    val skillLevel: SkillLevel,
    val venueId: String? = null,
    val proposedVenues: List<String> = emptyList(),
    val proposedTimeSlots: List<String> = emptyList(),
    val matchDate: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val expiresAt: String,
    val status: String,
    val totalCost: Int,
    val costSharing: String,
    val hostShare: Int? = null,
    val guestShare: Int? = null,
    val depositAmount: Int = 0,
    val depositHolder: String? = null,
    val notes: String = "",
    val createdAt: String,
)

/** In-memory store record for a Match A request (Debug only). */
data class StoreMatchRequest(
    val id: String,
    val matchId: String,
    val requesterTeamId: String,
    val conversationId: String,
    val status: String,
    val message: String,
    val createdAt: String,
)

/** In-memory store record for a Match B pairing room (Debug only). */
data class StorePairing(
    val id: String,
    val matchId: String,
    val conversationId: String,
    val hostTeamId: String,
    val guestTeamId: String,
    val status: String,
    val venueId: String,
    val timeSlot: String,
    val totalCost: Int,
    val costSharing: String,
    val depositAmount: Int,
    val depositHolder: String,
    val revision: Int,
    val hostConfirmed: Boolean,
    val guestConfirmed: Boolean,
    val createdAt: String,
)

/** In-memory store record for a mercenary recruitment post (Debug only). */
data class StoreRecruitment(
    val id: String,
    val teamId: String,
    val matchId: String? = null,
    val externalMatchId: String? = null,
    val position: PlayerPosition,
    val quantity: Int,
    val approvedCount: Int,
    val status: String,
    val area: String,
    val wardId: String,
    val distanceKm: Double,
    val matchDate: String,
    val timeSlot: String,
    val venueId: String,
    val skillLevel: SkillLevel,
    val note: String,
    val createdAt: String,
)

/** In-memory store record for a mercenary application (Debug only). */
data class StoreApplication(
    val id: String,
    val recruitmentId: String,
    val applicantId: String,
    val conversationId: String,
    val status: String,
    val message: String,
    val createdAt: String,
    val approvedAt: String? = null,
)

/** In-memory store record for an external match (Debug only). */
data class StoreExternalMatch(
    val id: String,
    val teamId: String,
    val title: String,
    val opponentName: String,
    val venueId: String,
    val matchDate: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val skillLevel: SkillLevel,
    val createdAt: String,
)

/** In-memory store record for a chat message (Debug only). */
data class StoreMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val timestamp: String,
    val readBy: List<String>,
)

/** In-memory store record for a conversation (Debug only). */
data class StoreConversation(
    val id: String,
    val contextType: String,
    val contextId: String,
    val title: String,
    val participantIds: List<String>,
    val canSend: Boolean,
    val readOnlyReason: String? = null,
    val messages: List<StoreMessage>,
)

/** In-memory store record for a notification (Debug only). */
data class StoreNotification(
    val id: String,
    val userId: String,
    val type: String,
    val title: String,
    val body: String,
    val referenceType: String?,
    val referenceId: String?,
    val read: Boolean,
    val createdAt: String,
)

/** In-memory store record for a post-match or mercenary review (Debug only). */
data class StoreReview(
    val id: String,
    val matchId: String,
    val reviewerId: String,
    val revieweeId: String,
    val reviewerType: String,
    val stars: Int,
    val comment: String,
    val noShow: Boolean,
    val scoreGiven: String? = null,
    val createdAt: String,
)

/** In-memory store record for a violation/venue/chat report (Debug only). */
data class StoreReport(
    val id: String,
    val reporterType: String,
    val reporterId: String,
    val reportedId: String,
    val matchId: String? = null,
    val type: String,
    val reason: String,
    val status: String,
    val createdAt: String,
    val canDisputeUntil: String? = null,
    val disputeNote: String? = null,
    val adminDecisionNote: String? = null,
)

/** In-memory store record for a sport in the catalog (Debug only). */
data class StoreSport(
    val id: String,
    val name: String,
    val active: Boolean,
)

/** Immutable snapshot of the entire ArenaFlow reactive in-memory store (Debug only). */
data class ArenaStoreSnapshot(
    val hasSeenOnboarding: Boolean = true,
    val currentUserId: String? = "user-cap-alpha",
    val actingTeamId: String? = "team-alpha",
    val isDeletedAccountBlocked: Boolean = false,
    val mockClockIso: String = "2026-11-18T10:00:00",
    val gpsState: GpsPermissionState = GpsPermissionState.GRANTED,
    val simulatedError: SimulatedError = SimulatedError.NONE,
    val pendingVenueSelection: SelectedVenueResult? = null,
    val users: Map<String, StoreUser> = emptyMap(),
    val wards: List<WardOption> = emptyList(),
    val teams: List<StoreTeam> = emptyList(),
    val teamMembers: List<StoreTeamMember> = emptyList(),
    val captainHistory: List<StoreCaptainHistory> = emptyList(),
    val invitations: List<StoreInvitation> = emptyList(),
    val venues: List<StoreVenue> = emptyList(),
    val venuePhotos: List<StoreVenuePhoto> = emptyList(),
    val matches: List<StoreMatch> = emptyList(),
    val matchRequests: List<StoreMatchRequest> = emptyList(),
    val pairings: List<StorePairing> = emptyList(),
    val recruitments: List<StoreRecruitment> = emptyList(),
    val applications: List<StoreApplication> = emptyList(),
    val externalMatches: List<StoreExternalMatch> = emptyList(),
    val conversations: List<StoreConversation> = emptyList(),
    val notifications: List<StoreNotification> = emptyList(),
    val reviews: List<StoreReview> = emptyList(),
    val reports: List<StoreReport> = emptyList(),
    val sports: List<StoreSport> = emptyList(),
)
