package com.igorwojda.showcase.app.presentation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.igorwojda.showcase.app.BuildConfig
import com.igorwojda.showcase.app.presentation.debug.DebugDemoControlOverlay
import com.igorwojda.showcase.app.presentation.debug.IS_DEMO_CONTROLS_AVAILABLE
import com.igorwojda.showcase.app.presentation.navigation.matchesGraph
import com.igorwojda.showcase.app.presentation.navigation.messagesGraph
import com.igorwojda.showcase.app.presentation.navigation.profileAndSharedGraph
import com.igorwojda.showcase.app.presentation.navigation.singleMatchGraph
import com.igorwojda.showcase.app.presentation.navigation.teamsGraph
import com.igorwojda.showcase.app.presentation.util.NavigationDestinationLogger
import com.igorwojda.showcase.feature.auth.presentation.screen.login.LoginMethodScreen
import com.igorwojda.showcase.feature.auth.presentation.screen.onboarding.OnboardingScreen
import com.igorwojda.showcase.feature.auth.presentation.screen.otp.OtpVerifyScreen
import com.igorwojda.showcase.feature.auth.presentation.screen.phone.PhoneInputScreen
import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import org.koin.compose.koinInject

/** Root ArenaFlow Compose entry handling Onboarding, Auth Gate, and 5-tab navigation shell. */
@Composable
fun MainShowcaseScreen(modifier: Modifier = Modifier) {
    val sessionRepository: SessionRepository = koinInject()
    val sessionState by sessionRepository.observeSession().collectAsStateWithLifecycle(ArenaSessionState())
    var isDemoOverlayOpen by rememberSaveable { mutableStateOf(false) }

    when {
        !sessionState.hasSeenOnboarding -> {
            OnboardingScreen(
                modifier = modifier,
                onComplete = {},
            )
        }

        sessionState.currentUser == null -> {
            AuthGateFlow(modifier = modifier)
        }

        else -> {
            AuthenticatedShell(
                sessionState = sessionState,
                sessionRepository = sessionRepository,
                onOpenDebugControls = {
                    if (IS_DEMO_CONTROLS_AVAILABLE) isDemoOverlayOpen = true
                },
                modifier = modifier,
            )
        }
    }

    if (IS_DEMO_CONTROLS_AVAILABLE) {
        DebugDemoControlOverlay(
            visible = isDemoOverlayOpen,
            onDismiss = { isDemoOverlayOpen = false },
        )
    }
}

@Composable
private fun AuthGateFlow(modifier: Modifier = Modifier) {
    val authNavController = rememberNavController()
    NavHost(
        navController = authNavController,
        startDestination = NavigationRoute.LoginMethod(),
        modifier = modifier.fillMaxSize(),
    ) {
        composable<NavigationRoute.LoginMethod> { entry ->
            val args = entry.toRoute<NavigationRoute.LoginMethod>()
            LoginMethodScreen(
                sessionExpired = args.sessionExpired,
                onPhoneLoginClick = { authNavController.navigate(NavigationRoute.PhoneInput) },
                onAuthSuccess = { _ -> },
            )
        }
        composable<NavigationRoute.PhoneInput> {
            PhoneInputScreen(
                onBackClick = { authNavController.popBackStack() },
                onOtpSend = { phone, expiresIn ->
                    authNavController.navigate(NavigationRoute.OtpVerify(phone, expiresIn))
                },
            )
        }
        composable<NavigationRoute.OtpVerify> { entry ->
            val args = entry.toRoute<NavigationRoute.OtpVerify>()
            OtpVerifyScreen(
                phone = args.phone,
                expiresInSeconds = args.expiresInSeconds,
                onBackClick = { authNavController.popBackStack() },
                onChangePhoneClick = { authNavController.popBackStack() },
                onAuthSuccess = { _ -> },
            )
        }
    }
}

@Composable
private fun AuthenticatedShell(
    sessionState: ArenaSessionState,
    sessionRepository: SessionRepository,
    onOpenDebugControls: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    if (BuildConfig.DEBUG) {
        NavigationDestinationLoggerEffect(navController)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isTopLevelDestination =
        currentDestination?.let { dest ->
            dest.hasRoute(NavigationRoute.MatchFeed::class) ||
                dest.hasRoute(NavigationRoute.MercenaryList::class) ||
                dest.hasRoute(NavigationRoute.TeamHub::class) ||
                dest.hasRoute(NavigationRoute.ChatInbox::class) ||
                dest.hasRoute(NavigationRoute.MyProfile::class)
        } ?: true

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (isTopLevelDestination) {
                BottomNavigationBar(
                    navController = navController,
                    unreadChatCount = sessionState.unreadMessages,
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationRoute.MatchesGraph,
            modifier =
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
        ) {
            matchesGraph(navController, sessionState, sessionRepository, onOpenDebugControls)
            singleMatchGraph(navController)
            teamsGraph(navController, onRequireLogin = {})
            messagesGraph(navController)
            profileAndSharedGraph(navController, sessionState, onRequireLogin = {})
        }
    }
}

@Composable
private fun NavigationDestinationLoggerEffect(navController: androidx.navigation.NavController) {
    androidx.compose.runtime.DisposableEffect(navController) {
        val listener =
            androidx.navigation.NavController.OnDestinationChangedListener { _, destination, arguments ->
                NavigationDestinationLogger.logDestinationChange(destination, arguments)
            }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }
}
