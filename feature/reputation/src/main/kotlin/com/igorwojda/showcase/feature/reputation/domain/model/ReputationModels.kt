package com.igorwojda.showcase.feature.reputation.domain.model

/** Report violation categories defined in REPUTATION-001 §4.1. */
enum class ReportType(
    val apiValue: String,
    val label: String,
) {
    NOSHOW("NOSHOW", "Bùng kèo (Không đến)"),
    BAD_PLAY("BAD_PLAY", "Chơi xấu"),
    BAD_COMMUNICATION("BAD_COMMUNICATION", "Giao tiếp tệ"),
    SKILL_MISMATCH("SKILL_MISMATCH", "Không đúng trình độ"),
    ;

    companion object {
        fun fromApiValue(value: String?): ReportType =
            entries.firstOrNull { it.apiValue.equals(value?.trim(), ignoreCase = true) } ?: BAD_COMMUNICATION
    }
}

/** Report lifecycle statuses defined in REPUTATION-001 §4.1. */
enum class ReportStatus(
    val apiValue: String,
    val label: String,
) {
    AUTO_ACCEPTED("AUTO_ACCEPTED", "Đã ghi nhận"),
    DISPUTED("DISPUTED", "Đang tranh chấp"),
    CONFIRMED("CONFIRMED", "Đã xác nhận"),
    REJECTED("REJECTED", "Đã bác bỏ"),
    ;

    companion object {
        fun fromApiValue(value: String?): ReportStatus =
            entries.firstOrNull { it.apiValue.equals(value?.trim(), ignoreCase = true) } ?: AUTO_ACCEPTED
    }
}

/** Direction of a report relative to the viewing team. */
enum class ReportDirection(
    val apiValue: String,
) {
    RECEIVED("RECEIVED"),
    SENT("SENT"),
    ;

    companion object {
        fun fromApiValue(value: String?): ReportDirection =
            entries.firstOrNull { it.apiValue.equals(value?.trim(), ignoreCase = true) } ?: RECEIVED
    }
}

/** Team reliability tier defined in REPUTATION-001 §4.1 and REP-04. */
enum class ReliabilityLevel(
    val apiValue: String,
    val label: String,
) {
    EXCELLENT("EXCELLENT", "Uy tín cao"),
    NORMAL("NORMAL", "Bình thường"),
    WARNING("WARNING", "Cảnh báo"),
    CRITICAL("CRITICAL", "Bị khóa (< 40)"),
    ;

    companion object {
        private const val CRITICAL_SCORE_THRESHOLD = 40

        fun fromApiValue(
            value: String?,
            score: Int? = null,
        ): ReliabilityLevel {
            if (score != null && score < CRITICAL_SCORE_THRESHOLD) {
                return CRITICAL
            }
            return entries.firstOrNull { it.apiValue.equals(value?.trim(), ignoreCase = true) } ?: NORMAL
        }
    }
}

/** Admin dispute resolution decision. */
enum class DisputeDecision(
    val apiValue: String,
    val label: String,
) {
    CONFIRMED("CONFIRMED", "Report đúng"),
    REJECTED("REJECTED", "Bác report"),
    ;

    companion object {
        fun fromApiValue(value: String?): DisputeDecision? = entries.firstOrNull { it.apiValue.equals(value?.trim(), ignoreCase = true) }
    }
}

/** Match outcome selection on the post-match report screen. */
enum class PostMatchOutcome(
    val label: String,
) {
    COMPLETED("Hoàn thành"),
    OPPONENT_NO_SHOW("Đối thủ không đến"),
    DISPUTED("Tranh chấp"),
    CANCELLED("Đã hủy"),
}

/** Compact counterpart team info in a report. */
data class CounterpartTeam(
    val teamId: String,
    val name: String,
    val avatarUrl: String?,
)

/** Post-match rating between two teams. */
data class MatchRating(
    val ratingId: String,
    val matchId: String,
    val raterTeamId: String,
    val rateeTeamId: String,
    val isNoShow: Boolean,
    val score: Int?,
    val comment: String?,
    val raterTeamScore: Int?,
    val rateeTeamScore: Int?,
    val createdAt: String,
    val matchStatus: String? = null,
)

/** Blind-rating response containing the team's given rating and optional received rating. */
data class MatchRatings(
    val matchId: String,
    val teamId: String,
    val given: MatchRating?,
    val received: MatchRating?,
)

/** Dispute attached to a NOSHOW report. */
data class ReportDispute(
    val disputeId: String,
    val status: String,
    val evidence: String?,
    val decision: DisputeDecision?,
    val resolutionNote: String?,
    val createdAt: String,
    val resolvedAt: String?,
)

/** Violation report between two teams. */
data class TeamReport(
    val reportId: String,
    val matchId: String,
    val matchStartsAt: String,
    val direction: ReportDirection,
    val counterpartTeam: CounterpartTeam,
    val reportType: ReportType,
    val reason: String,
    val status: ReportStatus,
    val disputeDeadlineAt: String?,
    val canDispute: Boolean,
    val reliabilityPenalty: Int?,
    val dispute: ReportDispute?,
    val createdAt: String,
)

/** Paginated list of team reports. */
data class TeamReportPage(
    val items: List<TeamReport>,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalItems: Int,
)

/** Result of submitting a NOSHOW dispute. */
data class DisputeSubmission(
    val disputeId: String,
    val reportId: String,
    val status: String,
    val evidence: String,
    val reportStatus: ReportStatus,
    val createdAt: String,
)

/** Match completion statistics for a team. */
data class CompletionStats(
    val completedMatches: Int,
    val countedMatches: Int,
    val completionRate: Int?,
    val isNewcomer: Boolean,
)

/** Average star rating summary for a team. */
data class RatingSummary(
    val averageRating: Double?,
    val ratingCount: Int,
)

/** Verification badge state for a team. */
data class VerificationBadge(
    val isVerified: Boolean,
    val verifiedAt: String?,
)

/** Full team reputation summary per REPUTATION-001 §2.9. */
data class TeamReputation(
    val teamId: String,
    val reliabilityScore: Int,
    val reliabilityLevel: ReliabilityLevel,
    val completion: CompletionStats,
    val rating: RatingSummary,
    val verification: VerificationBadge,
)

/** Input parameters for submitting a post-match rating. */
data class SubmitRatingParams(
    val matchId: String,
    val isNoShow: Boolean,
    val score: Int? = null,
    val comment: String? = null,
    val raterTeamScore: Int? = null,
    val rateeTeamScore: Int? = null,
)
