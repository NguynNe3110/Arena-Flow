package com.igorwojda.showcase.app.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.igorwojda.showcase.app.R
import com.igorwojda.showcase.feature.base.presentation.compose.theme.ShowcaseTheme

@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val navigationItems = getBottomNavigationItems() //

    val navBackStackEntry by navController.currentBackStackEntryAsState() //
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
    NavigationBar(
        modifier = modifier,
    ) {
        items.forEach { item ->
            val selected = isSelected(item)
            val contentColor =
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                Color.Transparent
                            },
                        ).selectable(
                            selected = selected,
//                        role = Role.Tab,
                            onClick = { onItemClick(item) },
                        ).heightIn(min = 60.dp)
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(item.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = contentColor,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(item.titleRes),
                    color = contentColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
            }
        }
    }
}

private fun getBottomNavigationItems() =
    listOf(
        BottomNavItem(
            R.string.bottom_navigation_match,
            R.drawable.ic_home,
            NavigationRoute.MatchesGraph,
        ),
        BottomNavItem(
            R.string.bottom_navigation_signle_match,
            R.drawable.ic_person,
            NavigationRoute.SingleMatchGraph,
        ),
        BottomNavItem(
            R.string.bottom_navigation_message,
            R.drawable.ic_message,
            NavigationRoute.MessagesGraph,
        ),
        BottomNavItem(
            R.string.bottom_navigation_myteam,
            R.drawable.ic_myteam,
            NavigationRoute.MyTeamsGraph,
        ),
    )

data class BottomNavItem(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    val route: NavigationRoute,
)

@PreviewLightDark
@Composable
private fun BottomNavigationBarPreview() {
    ShowcaseTheme {
        val items = getBottomNavigationItems()

        BottomNavigationBarContent(
            items = items,
            isSelected = { it == items.first() },
            onItemClick = {},
        )
    }
}
