package com.igorwojda.showcase.feature.base.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.repository.AuthTokenSnapshot
import com.igorwojda.showcase.feature.base.domain.repository.AuthTokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class SharedPrefsAuthTokenStore(
    context: Context,
) : AuthTokenStore {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val mutableSnapshot = MutableStateFlow(readSnapshot())
    override val authSnapshotFlow: StateFlow<AuthTokenSnapshot> = mutableSnapshot.asStateFlow()

    override fun current(): AuthTokenSnapshot = mutableSnapshot.value

    override fun saveTokens(
        userId: String,
        role: AccountRole,
        accessToken: String,
        refreshToken: String,
    ) {
        prefs
            .edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_ROLE, role.name)
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
        mutableSnapshot.value = readSnapshot()
    }

    override fun setHasSeenOnboarding(seen: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING, seen).apply()
        mutableSnapshot.value = readSnapshot()
    }

    override fun clearSession() {
        prefs
            .edit()
            .remove(KEY_USER_ID)
            .remove(KEY_ROLE)
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
        mutableSnapshot.value = readSnapshot()
    }

    private fun readSnapshot(): AuthTokenSnapshot =
        AuthTokenSnapshot(
            userId = prefs.getString(KEY_USER_ID, null),
            role = prefs.getString(KEY_ROLE, null)?.let { runCatching { AccountRole.valueOf(it) }.getOrNull() } ?: AccountRole.USER,
            accessToken = prefs.getString(KEY_ACCESS_TOKEN, null),
            refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null),
            hasSeenOnboarding = prefs.getBoolean(KEY_ONBOARDING, false),
        )

    private companion object {
        const val PREFS_NAME = "arenaflow_auth_prefs"
        const val KEY_USER_ID = "user_id"
        const val KEY_ROLE = "role"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_ONBOARDING = "has_seen_onboarding"
    }
}
