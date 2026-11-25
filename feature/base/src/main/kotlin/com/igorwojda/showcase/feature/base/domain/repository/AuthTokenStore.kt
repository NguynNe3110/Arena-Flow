package com.igorwojda.showcase.feature.base.domain.repository

import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import kotlinx.coroutines.flow.StateFlow

/** Persisted JWT token & onboarding flag store shared across network interceptors and features. */
interface AuthTokenStore {
    val authSnapshotFlow: StateFlow<AuthTokenSnapshot>

    fun current(): AuthTokenSnapshot

    fun saveTokens(
        userId: String,
        role: AccountRole,
        accessToken: String,
        refreshToken: String,
    )

    fun setHasSeenOnboarding(seen: Boolean)

    fun clearSession()
}

/** Snapshot of persisted auth tokens. */
data class AuthTokenSnapshot(
    val userId: String? = null,
    val role: AccountRole = AccountRole.USER,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val hasSeenOnboarding: Boolean = false,
) {
    val isAuthenticated: Boolean get() = !accessToken.isNullOrBlank() && !userId.isNullOrBlank()
}
