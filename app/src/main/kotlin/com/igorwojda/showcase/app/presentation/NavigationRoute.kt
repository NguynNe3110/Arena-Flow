package com.igorwojda.showcase.app.presentation

import kotlinx.serialization.Serializable

sealed interface NavigationRoute {
    // Tab graphs - each bottom navigation tab has its own nested graph (and back stack)
    @Serializable
    data object MatchesGraph : NavigationRoute

    @Serializable
    data object SingleMatchGraph : NavigationRoute

    @Serializable
    data object MessagesGraph : NavigationRoute

    @Serializable
    data object MyTeamsGraph : NavigationRoute

    // Match tab destinations
    @Serializable
    data object MatchList : NavigationRoute

    @Serializable
    data class MatchDetail(val matchId: String) : NavigationRoute

    // Placeholder destinations for subsequent tabs
    @Serializable
    data object SingleMatch : NavigationRoute

    @Serializable
    data object Messages : NavigationRoute

    @Serializable
    data object MyTeams : NavigationRoute
}
