@file:Suppress("TooManyFunctions", "MagicNumber")

package com.igorwojda.showcase.app.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.igorwojda.showcase.app.BuildConfig
import com.igorwojda.showcase.app.presentation.util.NavigationDestinationLogger
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BrandHeader
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.uzuu.match.presentation.screen.matchcreate.MatchCreateScreen
import com.uzuu.match.presentation.screen.matchdetail.MatchDetailScreen
import com.uzuu.match.presentation.screen.matchlist.MatchListScreen
import com.uzuu.match.presentation.screen.matchreview.MatchReviewScreen
import com.uzuu.mercenary.presentation.screen.mercenarylist.MercenaryListScreen
import com.uzuu.pairing.presentation.screen.pairingroom.PairingRoomScreen
import com.uzuu.team.presentation.screen.teamcreate.TeamCreateScreen
import com.uzuu.team.presentation.screen.teamdetail.TeamDetailScreen

@Composable
fun MainShowcaseScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    if (BuildConfig.DEBUG) {
        NavigationDestinationLoggerEffect(navController)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isTopLevelDestination =
        currentDestination?.let { dest ->
            dest.hasRoute(NavigationRoute.MatchList::class) ||
                dest.hasRoute(NavigationRoute.MercenaryList::class) ||
                dest.hasRoute(NavigationRoute.Messages::class) ||
                dest.hasRoute(NavigationRoute.TeamDetail::class) ||
                dest.hasRoute(NavigationRoute.MyTeams::class)
        } ?: true

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (isTopLevelDestination) {
                BrandHeader(
                    onNotificationClick = {
                        // Notifications action
                    },
                )
            }
        },
        bottomBar = { BottomNavigationBar(navController) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationRoute.MatchesGraph,
            modifier =
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
        ) {
            matchesGraph(navController)
            singleMatchGraph(navController)
            messagesGraph(navController)
            myTeamsGraph(navController)
        }
    }
}

private fun NavGraphBuilder.matchesGraph(navController: NavController) {
    navigation<NavigationRoute.MatchesGraph>(
        startDestination = NavigationRoute.MatchList,
    ) {
        composable<NavigationRoute.MatchList> {
            MatchListScreen(
                onMatchClick = { matchId ->
                    navController.navigate(NavigationRoute.MatchDetail(matchId))
                },
                onCreateMatchClick = {
                    navController.navigate(NavigationRoute.MatchCreate)
                },
            )
        }

        composable<NavigationRoute.MatchDetail> { entry ->
            val args = entry.toRoute<NavigationRoute.MatchDetail>()
            MatchDetailScreen(
                matchId = args.matchId,
                onBackClick = { navController.popBackStack() },
            )
        }

        composable<NavigationRoute.MatchCreate> {
            MatchCreateScreen(
                onNavigateToReview = {
                    navController.navigate(NavigationRoute.MatchReview)
                },
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }

        composable<NavigationRoute.MatchReview> {
            MatchReviewScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPostSuccess = {
                    navController.popBackStack(NavigationRoute.MatchList, inclusive = false)
                },
            )
        }

        composable<NavigationRoute.PairingRoom> { entry ->
            val args = entry.toRoute<NavigationRoute.PairingRoom>()
            PairingRoomScreen(
                pairingId = args.pairingId,
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}

private fun NavGraphBuilder.singleMatchGraph(navController: NavController) {
    navigation<NavigationRoute.SingleMatchGraph>(
        startDestination = NavigationRoute.MercenaryList,
    ) {
        composable<NavigationRoute.MercenaryList> {
            MercenaryListScreen(
                onPostClick = { /* post detail placeholder */ },
            )
        }
    }
}

private fun NavGraphBuilder.messagesGraph(navController: NavController) {
    navigation<NavigationRoute.MessagesGraph>(
        startDestination = NavigationRoute.Messages,
    ) {
        composable<NavigationRoute.Messages> {
            MessagesTabScreen()
        }

        composable<NavigationRoute.PairingRoom> { entry ->
            val args = entry.toRoute<NavigationRoute.PairingRoom>()
            PairingRoomScreen(
                pairingId = args.pairingId,
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}

private fun NavGraphBuilder.myTeamsGraph(navController: NavController) {
    navigation<NavigationRoute.MyTeamsGraph>(
        startDestination = NavigationRoute.TeamDetail(),
    ) {
        composable<NavigationRoute.TeamDetail> {
            TeamDetailScreen(
                onCreateTeamClick = {
                    navController.navigate(NavigationRoute.TeamCreate)
                },
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }

        composable<NavigationRoute.TeamCreate> {
            TeamCreateScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onTeamCreate = { _ ->
                    navController.popBackStack()
                },
            )
        }

        composable<NavigationRoute.MyTeams> {
            TeamDetailScreen(
                onCreateTeamClick = {
                    navController.navigate(NavigationRoute.TeamCreate)
                },
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
    }
}

@Composable
private fun MessagesTabScreen(modifier: Modifier = Modifier) {
    var messageText by rememberSaveable { mutableStateOf("") }
    val messages = remember { mutableStateListOf<String>() }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(ArenaColors.Background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
    ) {
        Text(
            text = "Tin nhắn",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextPrimary,
            letterSpacing = (-0.035).sp,
            lineHeight = 36.sp,
        )
        Text(
            text = "Thảo Điền FC · Đội trưởng",
            fontSize = 15.sp,
            color = ArenaColors.TextMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
        )

        // Incoming bubble
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE4EBE2),
            modifier = Modifier.fillMaxWidth(0.85f).padding(vertical = 6.dp),
        ) {
            Text(
                text = "Chào đội bạn, đội mình đá giao lưu vừa sức nhé!",
                modifier = Modifier.padding(14.dp),
                fontSize = 15.sp,
                color = ArenaColors.TextPrimary,
                lineHeight = 22.sp,
            )
        }

        // Outgoing bubble
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFD6ECDB),
                modifier = Modifier.fillMaxWidth(0.85f).padding(vertical = 6.dp),
            ) {
                Text(
                    text = "Đội mình trình trung bình, ưu tiên chơi đẹp.",
                    modifier = Modifier.padding(14.dp),
                    fontSize = 15.sp,
                    color = ArenaColors.TextPrimary,
                    lineHeight = 22.sp,
                )
            }
        }

        // Dynamically added messages
        messages.forEach { msg ->
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFD6ECDB),
                    modifier = Modifier.fillMaxWidth(0.85f).padding(vertical = 6.dp),
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(14.dp),
                        fontSize = 15.sp,
                        color = ArenaColors.TextPrimary,
                        lineHeight = 22.sp,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input label
        Text(
            text = "Tin nhắn",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = ArenaColors.TextPrimary,
            modifier = Modifier.padding(bottom = 7.dp),
        )
        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            placeholder = { Text("Nhắn với đội trưởng…", color = ArenaColors.TextMuted, fontSize = 15.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = ArenaShapes.Input,
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = ArenaColors.InputBorder,
                    unfocusedBorderColor = ArenaColors.InputBorder,
                    focusedTextColor = ArenaColors.TextPrimary,
                    unfocusedTextColor = ArenaColors.TextPrimary,
                ),
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                if (messageText.isNotBlank()) {
                    messages.add(messageText.trim())
                    messageText = ""
                }
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = ArenaShapes.Button,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = ArenaColors.Primary,
                    contentColor = Color.White,
                ),
        ) {
            Text("Gửi tin nhắn mẫu", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Tin nhắn thử trong phiên xem này.",
            fontSize = 13.sp,
            color = ArenaColors.TextMuted,
        )
    }
}

@Composable
private fun NavigationDestinationLoggerEffect(navController: NavController) {
    DisposableEffect(navController) {
        val listener =
            NavController.OnDestinationChangedListener { _, destination, arguments ->
                NavigationDestinationLogger.logDestinationChange(destination, arguments)
            }
        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
}
