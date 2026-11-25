package com.igorwojda.showcase.feature.base.domain.model

/** Demo persona roles matching Prototype v2 Demo Control Panel. */
enum class PersonaRole(
    val label: String,
) {
    GUEST("Khách (Chưa đăng nhập)"),
    NEW_USER("User mới (Chưa hoàn thiện hồ sơ)"),
    PLAYER("Cá nhân đá lẻ"),
    CAPTAIN("Đội trưởng (FC Sao Mai)"),
    MULTI_CAPTAIN("Đội trưởng nhiều đội"),
    MEMBER("Thành viên đội"),
    ADMIN("Quản trị viên (Admin)"),
}

/** Account-level system role per USER-001 §4.1. */
enum class AccountRole {
    USER,
    ADMIN,
}

/** Player preferred position with official Vietnamese API labels per USER-001 §4.1. */
enum class PlayerPosition(
    val label: String,
) {
    GOALKEEPER("Thủ môn"),
    DEFENDER("Hậu vệ"),
    MIDFIELDER("Tiền vệ"),
    FORWARD("Tiền đạo"),
    ;

    companion object {
        fun fromApiLabel(value: String?): PlayerPosition? =
            entries.firstOrNull { it.label.equals(value?.trim(), ignoreCase = true) || it.name.equals(value?.trim(), ignoreCase = true) }
    }
}

/** Official 9 skill/style labels per USER-001 §4.1 & MATCH-001 §4.1. */
enum class SkillLevel(
    val label: String,
) {
    BEGINNER("Vui chơi"),
    INTERMEDIATE("Rèn luyện"),
    ADVANCED("Thi đấu"),
    CHILL("Chill"),
    ENTHUSIASTIC("Nhiệt tình"),
    FIERY("Máu lửa"),
    NEWBIE("Mới bắt đầu"),
    EXPERIENCED("Đã có kinh nghiệm"),
    VETERAN("Lão làng"),
    ;

    companion object {
        fun fromApiLabel(value: String?): SkillLevel? =
            entries.firstOrNull { it.label.equals(value?.trim(), ignoreCase = true) || it.name.equals(value?.trim(), ignoreCase = true) }
    }
}

/** Pitch format options per MATCH-001 §4.1 & VENUE-001 §4.1. */
enum class PitchFormat(
    val label: String,
) {
    FIVE_A_SIDE("Sân 5"),
    SEVEN_A_SIDE("Sân 7"),
    ELEVEN_A_SIDE("Sân 11"),
    ;

    companion object {
        fun fromApiName(value: String?): PitchFormat? = entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) }
    }
}

/** Global simulated error modes for Debug testing. */
enum class SimulatedError(
    val code: String?,
    val label: String,
) {
    NONE(null, "Bình thường"),
    FORBIDDEN_403("403", "403 Không có quyền"),
    NOT_FOUND_404("404", "404 Không tìm thấy"),
    CONFLICT_409("409", "409 Xung đột phiên bản"),
    NETWORK("NET", "Mất kết nối mạng"),
}

/** GPS permission state for mercenary explore radius vs ward fallback. */
enum class GpsPermissionState {
    GRANTED,
    DENIED,
}

/** Administrative ward option. */
data class WardOption(
    val id: String,
    val name: String,
    val district: String,
    val city: String,
) {
    val fullLabel: String get() = "$name, $district"
}

/** Authenticated user summary exposed to all features via SessionRepository. */
data class SessionUserSummary(
    val id: String,
    val phone: String,
    val displayName: String,
    val preferredPosition: PlayerPosition?,
    val wardId: String?,
    val role: AccountRole,
    val isNewUser: Boolean,
    val reputationScore: Int,
    val matchCount: Int,
)

/** Summary of a team captained by the current user for TEAM-14 acting-team selector. */
data class ActingTeamOption(
    val id: String,
    val name: String,
    val wardName: String,
    val reputationScore: Int,
    val status: String,
)

/** Venue selection result returned from VEN-01/VEN-03 to caller forms (MATCH-05, EXT-01). */
data class SelectedVenueResult(
    val requestKey: String,
    val venueId: String,
    val venueName: String,
    val venueAddress: String,
)

/** Reactive session & demo control state observed by :app and features. */
data class ArenaSessionState(
    val hasSeenOnboarding: Boolean = true,
    val currentUser: SessionUserSummary? = null,
    val actingTeamId: String? = null,
    val captainedTeams: List<ActingTeamOption> = emptyList(),
    val unreadNotifications: Int = 0,
    val unreadMessages: Int = 0,
    val mockClockIso: String = "2026-11-18T10:00:00",
    val gpsState: GpsPermissionState = GpsPermissionState.GRANTED,
    val simulatedError: SimulatedError = SimulatedError.NONE,
    val pendingVenueSelection: SelectedVenueResult? = null,
    val isDeletedAccountBlocked: Boolean = false,
) {
    val actingTeam: ActingTeamOption?
        get() = captainedTeams.firstOrNull { it.id == actingTeamId } ?: captainedTeams.firstOrNull()
}
