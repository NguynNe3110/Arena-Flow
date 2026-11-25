package com.igorwojda.showcase.app.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.igorwojda.showcase.app.R
import com.igorwojda.showcase.feature.base.presentation.compose.theme.ShowcaseTheme
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors

private const val MAX_BADGE_COUNT = 99

@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier,
    unreadChatCount: Int = 0,
) {
    val navigationItems = getBottomNavigationItems(unreadChatCount)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    BottomNavigationBarContent(
        items = navigationItems,
        isSelected = { item ->
            currentDestination?.hierarchy?.any {
                it.hasRoute(item.route::class)
            } == true
        },
        onItemClick = { item ->
            navController.navigate(item.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun BottomNavigationBarContent(
    items: List<BottomNavItem>,
    isSelected: (BottomNavItem) -> Boolean,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            val selected = isSelected(item)
            val contentColor =
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                Color.Transparent
                            },
                        ).selectable(
                            selected = selected,
                            onClick = { onItemClick(item) },
                        ).heightIn(min = 60.dp)
                        .padding(horizontal = 2.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                BottomNavIconWithBadge(
                    iconRes = item.iconRes,
                    badgeCount = item.badgeCount,
                    contentColor = contentColor,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(item.titleRes),
                    color = contentColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun BottomNavIconWithBadge(
    @DrawableRes iconRes: Int,
    badgeCount: Int,
    contentColor: Color,
) {
    Box(contentAlignment = Alignment.TopEnd) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = contentColor,
        )

        if (badgeCount > 0) {
            Box(
                modifier =
                    Modifier
                        .offset(x = 6.dp, y = (-4).dp)
                        .clip(CircleShape)
                        .background(ArenaColors.Error)
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (badgeCount > MAX_BADGE_COUNT) "99+" else badgeCount.toString(),
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun getBottomNavigationItems(unreadChatCount: Int) =
    listOf(
        BottomNavItem(
            titleRes = R.string.bottom_navigation_match,
            iconRes = R.drawable.ic_home,
            route = NavigationRoute.MatchesGraph,
        ),
        BottomNavItem(
            titleRes = R.string.bottom_navigation_single_match,
            iconRes = R.drawable.ic_favorite,
            route = NavigationRoute.SingleMatchGraph,
        ),
        BottomNavItem(
            titleRes = R.string.bottom_navigation_myteam,
            iconRes = R.drawable.ic_myteam,
            route = NavigationRoute.TeamsGraph,
        ),
        BottomNavItem(
            titleRes = R.string.bottom_navigation_message,
            iconRes = R.drawable.ic_message,
            route = NavigationRoute.MessagesGraph,
            badgeCount = unreadChatCount,
        ),
        BottomNavItem(
            titleRes = R.string.bottom_navigation_profile,
            iconRes = R.drawable.ic_person,
            route = NavigationRoute.ProfileGraph,
        ),
    )

data class BottomNavItem(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    val route: NavigationRoute,
    val badgeCount: Int = 0,
)

@PreviewLightDark
@Composable
private fun BottomNavigationBarPreview() {
    ShowcaseTheme {
        val items = getBottomNavigationItems(unreadChatCount = 3)
        BottomNavigationBarContent(
            items = items,
            isSelected = { it == items.first() },
            onItemClick = {},
        )
    }
}
