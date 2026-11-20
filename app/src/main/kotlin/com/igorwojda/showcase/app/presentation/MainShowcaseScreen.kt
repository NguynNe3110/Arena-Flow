package com.igorwojda.showcase.app.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.igorwojda.showcase.app.BuildConfig
import com.igorwojda.showcase.app.R
import com.igorwojda.showcase.app.presentation.util.NavigationDestinationLogger
import com.uzuu.match.presentation.screen.matchdetail.MatchDetailScreen
import com.uzuu.match.presentation.screen.matchlist.MatchListScreen

@Composable
fun MainShowcaseScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    if (BuildConfig.DEBUG) {
        NavigationDestinationLoggerEffect(navController)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
            singleMatchGraph()
            messagesGraph()
            myTeamsGraph()
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
            )
        }

        composable<NavigationRoute.MatchDetail> { entry ->
            val args = entry.toRoute<NavigationRoute.MatchDetail>()
            MatchDetailScreen(
                matchId = args.matchId,
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}

private fun NavGraphBuilder.singleMatchGraph() {
    navigation<NavigationRoute.SingleMatchGraph>(
        startDestination = NavigationRoute.SingleMatch,
    ) {
        composable<NavigationRoute.SingleMatch> {
            PlaceholderTabScreen(title = stringResource(R.string.bottom_navigation_signle_match))
        }
    }
}

private fun NavGraphBuilder.messagesGraph() {
    navigation<NavigationRoute.MessagesGraph>(
        startDestination = NavigationRoute.Messages,
    ) {
        composable<NavigationRoute.Messages> {
            PlaceholderTabScreen(title = stringResource(R.string.bottom_navigation_message))
        }
    }
}

private fun NavGraphBuilder.myTeamsGraph() {
    navigation<NavigationRoute.MyTeamsGraph>(
        startDestination = NavigationRoute.MyTeams,
    ) {
        composable<NavigationRoute.MyTeams> {
            PlaceholderTabScreen(title = stringResource(R.string.bottom_navigation_myteam))
        }
    }
}

@Composable
private fun PlaceholderTabScreen(
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
