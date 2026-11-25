@file:Suppress("CognitiveComplexMethod", "LongMethod", "TooManyFunctions", "UnusedImports")

package com.igorwojda.showcase.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.igorwojda.showcase.app.presentation.NavigationRoute
import com.igorwojda.showcase.feature.admin.presentation.screen.adminconsole.AdminConsoleScreen
import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.model.SelectedVenueResult
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.chat.presentation.screen.inbox.ChatInboxScreen
import com.igorwojda.showcase.feature.chat.presentation.screen.room.ChatRoomScreen
import com.igorwojda.showcase.feature.match.presentation.screen.matchcreate.MatchCreateScreen
import com.igorwojda.showcase.feature.match.presentation.screen.matchdetail.MatchDetailScreen
import com.igorwojda.showcase.feature.match.presentation.screen.matchfeed.MatchFeedScreen
import com.igorwojda.showcase.feature.match.presentation.screen.matchreview.MatchReviewScreen
import com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarycreate.MercenaryCreateScreen
import com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarydetail.MercenaryDetailScreen
import com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarylist.MercenaryListScreen
import com.igorwojda.showcase.feature.notification.presentation.screen.notificationlist.NotificationListRoute
import com.igorwojda.showcase.feature.pairing.presentation.screen.pairingroom.PairingRoomScreen
import com.igorwojda.showcase.feature.profile.presentation.screen.accountmenu.AccountMenuScreen
import com.igorwojda.showcase.feature.profile.presentation.screen.myprofile.MyProfileScreen
import com.igorwojda.showcase.feature.profile.presentation.screen.profileedit.ProfileEditScreen
import com.igorwojda.showcase.feature.reputation.presentation.screen.postmatchreport.PostMatchReportScreen
import com.igorwojda.showcase.feature.reputation.presentation.screen.reputationhistory.ReputationHistoryScreen
import com.igorwojda.showcase.feature.team.presentation.screen.teamdetail.TeamDetailScreen
import com.igorwojda.showcase.feature.team.presentation.screen.teamform.TeamFormScreen
import com.igorwojda.showcase.feature.team.presentation.screen.teamhistory.TeamHistoryScreen
import com.igorwojda.showcase.feature.team.presentation.screen.teamhub.TeamHubScreen
import com.igorwojda.showcase.feature.team.presentation.screen.teaminvitation.TeamInvitationScreen
import com.igorwojda.showcase.feature.team.presentation.screen.teammanage.TeamManageScreen
import com.igorwojda.showcase.feature.venue.presentation.screen.venuedetail.VenueDetailScreen
import com.igorwojda.showcase.feature.venue.presentation.screen.venueform.VenueFormScreen
import com.igorwojda.showcase.feature.venue.presentation.screen.venuephotos.VenuePhotosScreen
import com.igorwojda.showcase.feature.venue.presentation.screen.venuepicker.VenuePickerScreen

internal fun NavGraphBuilder.matchesGraph(
    navController: NavController,
    sessionState: ArenaSessionState,
    sessionRepository: SessionRepository,
    onOpenDebugControls: () -> Unit,
) {
    navigation<NavigationRoute.MatchesGraph>(startDestination = NavigationRoute.MatchFeed) {
        composable<NavigationRoute.MatchFeed> {
            MatchFeedScreen(
                onMatchClick = { navController.navigate(NavigationRoute.MatchDetail(it)) },
                onCreateMatchClick = { navController.navigate(NavigationRoute.MatchCreate()) },
                onNotificationClick = { navController.navigate(NavigationRoute.NotificationList) },
                onDebugClick = onOpenDebugControls,
            )
        }
        composable<NavigationRoute.MatchDetail> { entry ->
            val args = entry.toRoute<NavigationRoute.MatchDetail>()
            val teamId = sessionState.actingTeamId.orEmpty()
            MatchDetailScreen(
                matchId = args.matchId,
                onBackClick = { navController.popBackStack() },
                onEditMatchClick = { navController.navigate(NavigationRoute.MatchCreate(it)) },
                onOpenPairingClick = { navController.navigate(NavigationRoute.PairingRoom(it)) },
                onOpenChatClick = { navController.navigate(NavigationRoute.ChatRoom(it)) },
                onOpenReportClick = { navController.navigate(NavigationRoute.PostMatchReport(it, teamId)) },
            )
        }
        composable<NavigationRoute.MatchCreate> { entry ->
            val args = entry.toRoute<NavigationRoute.MatchCreate>()
            MatchCreateScreen(
                matchId = args.matchId,
                onBackClick = { navController.popBackStack() },
                onOpenVenuePicker = { key -> navController.navigate(NavigationRoute.VenuePicker(key)) },
                onNavigateToReview = { navController.navigate(NavigationRoute.MatchReview) },
                onSavedSuccess = { matchId ->
                    navController.popBackStack()
                    navController.navigate(NavigationRoute.MatchDetail(matchId))
                },
            )
        }
        composable<NavigationRoute.MatchReview> {
            MatchReviewScreen(
                onBackClick = { navController.popBackStack() },
                onPostSuccess = { navController.popBackStack(NavigationRoute.MatchFeed, inclusive = false) },
            )
        }
        composable<NavigationRoute.PairingRoom> { entry ->
            val args = entry.toRoute<NavigationRoute.PairingRoom>()
            val requestKey = "pairing_${args.roomId}"
            var pickedVenue by remember(args.roomId) { mutableStateOf<SelectedVenueResult?>(null) }
            LaunchedEffect(entry) {
                val consumed = sessionRepository.consumePendingVenueSelection(requestKey)
                if (consumed != null) pickedVenue = consumed
            }
            PairingRoomScreen(
                roomId = args.roomId,
                onBackClick = { navController.popBackStack() },
                onOpenVenuePicker = { navController.navigate(NavigationRoute.VenuePicker(requestKey)) },
                onOpenChat = { navController.navigate(NavigationRoute.ChatRoom(it)) },
                onOpenPostMatchReport = { matchId ->
                    navController.navigate(NavigationRoute.PostMatchReport(matchId, sessionState.actingTeamId.orEmpty()))
                },
                selectedVenueFromPicker = pickedVenue,
            )
        }
    }
}

internal fun NavGraphBuilder.singleMatchGraph(navController: NavController) {
    navigation<NavigationRoute.SingleMatchGraph>(startDestination = NavigationRoute.MercenaryList) {
        composable<NavigationRoute.MercenaryList> {
            MercenaryListScreen(
                onPostClick = { navController.navigate(NavigationRoute.MercenaryDetail(it)) },
                onCreatePostClick = { navController.navigate(NavigationRoute.MercenaryCreate()) },
                onNotificationClick = { navController.navigate(NavigationRoute.NotificationList) },
            )
        }
        composable<NavigationRoute.MercenaryDetail> { entry ->
            val args = entry.toRoute<NavigationRoute.MercenaryDetail>()
            MercenaryDetailScreen(
                postId = args.postId,
                onBackClick = { navController.popBackStack() },
                onEditPostClick = { navController.navigate(NavigationRoute.MercenaryCreate(it)) },
                onOpenChatClick = { navController.navigate(NavigationRoute.ChatRoom(it)) },
            )
        }
        composable<NavigationRoute.MercenaryCreate> { entry ->
            val args = entry.toRoute<NavigationRoute.MercenaryCreate>()
            MercenaryCreateScreen(
                postId = args.postId,
                onBackClick = { navController.popBackStack() },
                onPickVenueClick = { key -> navController.navigate(NavigationRoute.VenuePicker(key)) },
                onSavedSuccess = { postId ->
                    navController.popBackStack()
                    navController.navigate(NavigationRoute.MercenaryDetail(postId))
                },
            )
        }
    }
}

internal fun NavGraphBuilder.teamsGraph(
    navController: NavController,
    onRequireLogin: () -> Unit,
) {
    navigation<NavigationRoute.TeamsGraph>(startDestination = NavigationRoute.TeamHub) {
        composable<NavigationRoute.TeamHub> {
            TeamHubScreen(
                onCreateTeamClick = { navController.navigate(NavigationRoute.TeamForm()) },
                onTeamClick = { navController.navigate(NavigationRoute.TeamDetail(it)) },
                onOpenInvitationClick = { navController.navigate(NavigationRoute.TeamInvitation(it)) },
                onLoginClick = onRequireLogin,
            )
        }
        composable<NavigationRoute.TeamDetail> { entry ->
            val args = entry.toRoute<NavigationRoute.TeamDetail>()
            TeamDetailScreen(
                teamId = args.teamId,
                onBackClick = { navController.popBackStack() },
                onEditTeamClick = { navController.navigate(NavigationRoute.TeamForm(it)) },
                onManageTeamClick = { navController.navigate(NavigationRoute.TeamManage(it)) },
                onHistoryClick = { navController.navigate(NavigationRoute.TeamHistory(it)) },
                onReputationClick = { navController.navigate(NavigationRoute.ReputationHistory(it)) },
                onScheduleClick = { navController.navigate(NavigationRoute.MatchesGraph) },
                onExternalMatchesClick = { navController.navigate(NavigationRoute.MatchesGraph) },
                onRecruitmentsClick = { navController.navigate(NavigationRoute.SingleMatchGraph) },
            )
        }
        composable<NavigationRoute.TeamForm> { entry ->
            val args = entry.toRoute<NavigationRoute.TeamForm>()
            TeamFormScreen(
                teamId = args.teamId,
                onBackClick = { navController.popBackStack() },
                onSaveComplete = { teamId ->
                    navController.popBackStack()
                    navController.navigate(NavigationRoute.TeamDetail(teamId))
                },
            )
        }
        composable<NavigationRoute.TeamManage> { entry ->
            val args = entry.toRoute<NavigationRoute.TeamManage>()
            TeamManageScreen(
                teamId = args.teamId,
                onBackClick = { navController.popBackStack() },
                onEditTeamClick = { navController.navigate(NavigationRoute.TeamForm(it)) },
                onOpenTeamDetailClick = { navController.navigate(NavigationRoute.TeamDetail(it)) },
                onHistoryClick = { navController.navigate(NavigationRoute.TeamHistory(it)) },
                onScheduleClick = { navController.navigate(NavigationRoute.MatchesGraph) },
                onExternalMatchesClick = { navController.navigate(NavigationRoute.MatchesGraph) },
                onRecruitmentsClick = { navController.navigate(NavigationRoute.SingleMatchGraph) },
            )
        }
        composable<NavigationRoute.TeamHistory> { entry ->
            val args = entry.toRoute<NavigationRoute.TeamHistory>()
            TeamHistoryScreen(teamId = args.teamId, onBackClick = { navController.popBackStack() })
        }
        composable<NavigationRoute.TeamInvitation> { entry ->
            val args = entry.toRoute<NavigationRoute.TeamInvitation>()
            TeamInvitationScreen(
                invitationId = args.invitationId,
                onBackClick = { navController.popBackStack() },
                onAcceptComplete = { teamId ->
                    navController.popBackStack()
                    navController.navigate(NavigationRoute.TeamDetail(teamId))
                },
            )
        }
    }
}

internal fun NavGraphBuilder.messagesGraph(navController: NavController) {
    navigation<NavigationRoute.MessagesGraph>(startDestination = NavigationRoute.ChatInbox) {
        composable<NavigationRoute.ChatInbox> {
            ChatInboxScreen(onRoomClick = { navController.navigate(NavigationRoute.ChatRoom(it)) })
        }
        composable<NavigationRoute.ChatRoom> { entry ->
            val args = entry.toRoute<NavigationRoute.ChatRoom>()
            ChatRoomScreen(
                roomId = args.roomId,
                onBackClick = { navController.popBackStack() },
                onNavigateToMatch = { navController.navigate(NavigationRoute.MatchDetail(it)) },
                onNavigateToPairing = { navController.navigate(NavigationRoute.PairingRoom(it)) },
                onNavigateToMercenary = { navController.navigate(NavigationRoute.MercenaryDetail(it)) },
            )
        }
    }
}

internal fun NavGraphBuilder.profileAndSharedGraph(
    navController: NavController,
    sessionState: ArenaSessionState,
    onRequireLogin: () -> Unit,
) {
    navigation<NavigationRoute.ProfileGraph>(startDestination = NavigationRoute.MyProfile) {
        composable<NavigationRoute.MyProfile> {
            val activeTeamId = sessionState.actingTeamId.orEmpty()
            MyProfileScreen(
                onEditProfileClick = { navController.navigate(NavigationRoute.ProfileEdit(false)) },
                onAccountMenuClick = { navController.navigate(NavigationRoute.AccountMenu) },
                onReputationClick = {
                    if (activeTeamId.isNotBlank()) {
                        navController.navigate(NavigationRoute.ReputationHistory(activeTeamId))
                    }
                },
                onReportsClick = {
                    if (activeTeamId.isNotBlank()) {
                        navController.navigate(NavigationRoute.ReputationHistory(activeTeamId))
                    }
                },
                onTeamClick = { navController.navigate(NavigationRoute.TeamDetail(it)) },
                onMyApplicationsClick = { navController.navigate(NavigationRoute.SingleMatchGraph) },
                onAdminClick = { navController.navigate(NavigationRoute.AdminConsole) },
                onLoginClick = onRequireLogin,
            )
        }
        composable<NavigationRoute.ProfileEdit> { entry ->
            val args = entry.toRoute<NavigationRoute.ProfileEdit>()
            ProfileEditScreen(
                isFirstTime = args.isFirstTime,
                onBackClick = { navController.popBackStack() },
                onSaveComplete = { navController.popBackStack() },
            )
        }
        composable<NavigationRoute.AccountMenu> {
            val activeTeamId = sessionState.actingTeamId.orEmpty()
            AccountMenuScreen(
                onBackClick = { navController.popBackStack() },
                onEditProfileClick = { navController.navigate(NavigationRoute.ProfileEdit(false)) },
                onReputationClick = {
                    if (activeTeamId.isNotBlank()) {
                        navController.navigate(NavigationRoute.ReputationHistory(activeTeamId))
                    }
                },
                onReportsClick = {
                    if (activeTeamId.isNotBlank()) {
                        navController.navigate(NavigationRoute.ReputationHistory(activeTeamId))
                    }
                },
                onMyTeamsClick = { navController.navigate(NavigationRoute.TeamsGraph) },
                onNotificationsClick = { navController.navigate(NavigationRoute.NotificationList) },
                onAdminClick = { navController.navigate(NavigationRoute.AdminConsole) },
                onOpenTeam = { navController.navigate(NavigationRoute.TeamDetail(it)) },
                onLogoutComplete = onRequireLogin,
                onAccountDeleteComplete = onRequireLogin,
            )
        }
    }

    sharedOverlayDestinations(navController, sessionState)
}

private fun NavGraphBuilder.sharedOverlayDestinations(
    navController: NavController,
    sessionState: ArenaSessionState,
) {
    composable<NavigationRoute.VenuePicker> { entry ->
        val args = entry.toRoute<NavigationRoute.VenuePicker>()
        VenuePickerScreen(
            requestKey = args.requestKey,
            onBackClick = { navController.popBackStack() },
            onOpenDetail = { venueId -> navController.navigate(NavigationRoute.VenueDetail(venueId, args.requestKey)) },
            onCreateVenue = { key -> navController.navigate(NavigationRoute.VenueForm(returnKey = key)) },
            onVenueSelectedBack = { navController.popBackStack() },
            onNotificationClick = { navController.navigate(NavigationRoute.NotificationList) },
            onProfileClick = { navController.navigate(NavigationRoute.ProfileGraph) },
        )
    }
    composable<NavigationRoute.VenueDetail> { entry ->
        val args = entry.toRoute<NavigationRoute.VenueDetail>()
        VenueDetailScreen(
            venueId = args.venueId,
            requestKey = args.requestKey,
            onBackClick = { navController.popBackStack() },
            onEditVenue = { id -> navController.navigate(NavigationRoute.VenueForm(venueId = id)) },
            onOpenPhotos = { id -> navController.navigate(NavigationRoute.VenuePhotos(id)) },
            onVenueSelectedBack = { navController.popBackStack() },
            onNotificationClick = { navController.navigate(NavigationRoute.NotificationList) },
            onProfileClick = { navController.navigate(NavigationRoute.ProfileGraph) },
        )
    }
    composable<NavigationRoute.VenueForm> { entry ->
        val args = entry.toRoute<NavigationRoute.VenueForm>()
        VenueFormScreen(
            venueId = args.venueId,
            returnKey = args.returnKey,
            onBackClick = { navController.popBackStack() },
            onSavedVenueDetail = { id ->
                navController.popBackStack()
                navController.navigate(NavigationRoute.VenueDetail(id))
            },
            onReturnSelection = { navController.popBackStack() },
            onNotificationClick = { navController.navigate(NavigationRoute.NotificationList) },
            onProfileClick = { navController.navigate(NavigationRoute.ProfileGraph) },
        )
    }
    composable<NavigationRoute.VenuePhotos> { entry ->
        val args = entry.toRoute<NavigationRoute.VenuePhotos>()
        VenuePhotosScreen(
            venueId = args.venueId,
            onBackClick = { navController.popBackStack() },
            onNotificationClick = { navController.navigate(NavigationRoute.NotificationList) },
            onProfileClick = { navController.navigate(NavigationRoute.ProfileGraph) },
        )
    }
    composable<NavigationRoute.NotificationList> {
        NotificationListRoute(
            userName = sessionState.currentUser?.displayName ?: "ArenaFlow",
            onBackClick = { navController.popBackStack() },
            onProfileClick = { navController.navigate(NavigationRoute.ProfileGraph) },
            onNavigateByTarget = { targetType, targetId ->
                navigateByNotificationTarget(navController, targetType, targetId)
            },
        )
    }
    composable<NavigationRoute.PostMatchReport> { entry ->
        val args = entry.toRoute<NavigationRoute.PostMatchReport>()
        PostMatchReportScreen(matchId = args.matchId, teamId = args.teamId, onBackClick = { navController.popBackStack() })
    }
    composable<NavigationRoute.ReputationHistory> { entry ->
        val args = entry.toRoute<NavigationRoute.ReputationHistory>()
        ReputationHistoryScreen(teamId = args.teamId, onBackClick = { navController.popBackStack() })
    }
    composable<NavigationRoute.AdminConsole> {
        AdminConsoleScreen(onBackClick = { navController.popBackStack() })
    }
}

private fun navigateByNotificationTarget(
    navController: NavController,
    targetType: String,
    targetId: String,
) {
    when (targetType.uppercase()) {
        "MATCH" -> navController.navigate(NavigationRoute.MatchDetail(targetId))
        "PAIRING" -> navController.navigate(NavigationRoute.PairingRoom(targetId))
        "MERCENARY" -> navController.navigate(NavigationRoute.MercenaryDetail(targetId))
        "TEAM" -> navController.navigate(NavigationRoute.TeamDetail(targetId))
        "CHAT" -> navController.navigate(NavigationRoute.ChatRoom(targetId))
        "VENUE" -> navController.navigate(NavigationRoute.VenueDetail(targetId))
        else -> navController.popBackStack()
    }
}
