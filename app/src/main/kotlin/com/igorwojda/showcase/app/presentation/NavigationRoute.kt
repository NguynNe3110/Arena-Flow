package com.igorwojda.showcase.app.presentation

import kotlinx.serialization.Serializable

/** Type-safe navigation destinations across the ArenaFlow shell. */
sealed interface NavigationRoute {
    // 5 top-level bottom navigation tab graphs
    @Serializable
    data object MatchesGraph : NavigationRoute

    @Serializable
    data object SingleMatchGraph : NavigationRoute

    @Serializable
    data object TeamsGraph : NavigationRoute

    @Serializable
    data object MessagesGraph : NavigationRoute

    @Serializable
    data object ProfileGraph : NavigationRoute

    // Auth / Launch gate destinations
    @Serializable
    data class LoginMethod(
        val sessionExpired: Boolean = false,
    ) : NavigationRoute

    @Serializable
    data object PhoneInput : NavigationRoute

    @Serializable
    data class OtpVerify(
        val phone: String,
        val expiresInSeconds: Int,
    ) : NavigationRoute

    // Match & Pairing destinations
    @Serializable
    data object MatchFeed : NavigationRoute

    @Serializable
    data class MatchDetail(
        val matchId: String,
    ) : NavigationRoute

    @Serializable
    data class MatchCreate(
        val matchId: String? = null,
    ) : NavigationRoute

    @Serializable
    data object MatchReview : NavigationRoute

    @Serializable
    data class PairingRoom(
        val roomId: String,
    ) : NavigationRoute

    // Mercenary destinations
    @Serializable
    data object MercenaryList : NavigationRoute

    @Serializable
    data class MercenaryDetail(
        val postId: String,
    ) : NavigationRoute

    @Serializable
    data class MercenaryCreate(
        val postId: String? = null,
    ) : NavigationRoute

    // Team destinations
    @Serializable
    data object TeamHub : NavigationRoute

    @Serializable
    data class TeamDetail(
        val teamId: String,
    ) : NavigationRoute

    @Serializable
    data class TeamForm(
        val teamId: String? = null,
    ) : NavigationRoute

    @Serializable
    data class TeamManage(
        val teamId: String,
    ) : NavigationRoute

    @Serializable
    data class TeamHistory(
        val teamId: String,
    ) : NavigationRoute

    @Serializable
    data class TeamInvitation(
        val invitationId: String? = null,
    ) : NavigationRoute

    // Venue destinations
    @Serializable
    data class VenuePicker(
        val requestKey: String? = null,
    ) : NavigationRoute

    @Serializable
    data class VenueDetail(
        val venueId: String,
        val requestKey: String? = null,
    ) : NavigationRoute

    @Serializable
    data class VenueForm(
        val venueId: String? = null,
        val returnKey: String? = null,
    ) : NavigationRoute

    @Serializable
    data class VenuePhotos(
        val venueId: String,
    ) : NavigationRoute

    // Chat destinations
    @Serializable
    data object ChatInbox : NavigationRoute

    @Serializable
    data class ChatRoom(
        val roomId: String,
    ) : NavigationRoute

    // Notification destination
    @Serializable
    data object NotificationList : NavigationRoute

    // Profile, Reputation & Admin destinations
    @Serializable
    data object MyProfile : NavigationRoute

    @Serializable
    data class ProfileEdit(
        val isFirstTime: Boolean = false,
    ) : NavigationRoute

    @Serializable
    data object AccountMenu : NavigationRoute

    @Serializable
    data class PostMatchReport(
        val matchId: String,
        val teamId: String,
    ) : NavigationRoute

    @Serializable
    data class ReputationHistory(
        val teamId: String,
    ) : NavigationRoute

    @Serializable
    data object AdminConsole : NavigationRoute
}
