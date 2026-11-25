package com.igorwojda.showcase.feature.base.data.repository

import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.model.GpsPermissionState
import com.igorwojda.showcase.feature.base.domain.model.PersonaRole
import com.igorwojda.showcase.feature.base.domain.model.SelectedVenueResult
import com.igorwojda.showcase.feature.base.domain.model.SessionUserSummary
import com.igorwojda.showcase.feature.base.domain.model.SimulatedError
import com.igorwojda.showcase.feature.base.domain.model.WardOption
import com.igorwojda.showcase.feature.base.domain.repository.AuthTokenStore
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/**
 * Release SessionRepository backed strictly by persisted AuthTokenStore and real API state.
 * Contains zero mock data or demo personas.
 */
internal class ReleaseSessionRepositoryImpl(
    private val authTokenStore: AuthTokenStore,
) : SessionRepository {
    private val mutableUiState = MutableStateFlow(ArenaSessionState(hasSeenOnboarding = authTokenStore.current().hasSeenOnboarding))

    override fun observeSession(): Flow<ArenaSessionState> =
        combine(authTokenStore.authSnapshotFlow, mutableUiState) { tokenSnapshot, ui ->
            val resolvedUser =
                if (tokenSnapshot.isAuthenticated) {
                    ui.currentUser ?: SessionUserSummary(
                        id = tokenSnapshot.userId.orEmpty(),
                        phone = "",
                        displayName = "Người dùng",
                        preferredPosition = null,
                        wardId = null,
                        role = tokenSnapshot.role,
                        isNewUser = false,
                        reputationScore = 80,
                        matchCount = 0,
                    )
                } else {
                    null
                }
            ui.copy(
                hasSeenOnboarding = tokenSnapshot.hasSeenOnboarding,
                currentUser = resolvedUser,
            )
        }

    override fun getWards(): List<WardOption> = emptyList()

    override suspend fun completeOnboarding() {
        authTokenStore.setHasSeenOnboarding(true)
    }

    override suspend fun switchPersona(role: PersonaRole) = Unit

    override suspend fun setActingTeam(teamId: String) {
        mutableUiState.value = mutableUiState.value.copy(actingTeamId = teamId)
    }

    override suspend fun updateDemoControls(
        gpsState: GpsPermissionState?,
        simulatedError: SimulatedError?,
        mockClockIso: String?,
    ) = Unit

    override suspend fun setPendingVenueSelection(result: SelectedVenueResult) {
        mutableUiState.value = mutableUiState.value.copy(pendingVenueSelection = result)
    }

    override suspend fun consumePendingVenueSelection(requestKey: String): SelectedVenueResult? {
        val pending = mutableUiState.value.pendingVenueSelection
        if (pending != null && pending.requestKey == requestKey) {
            mutableUiState.value = mutableUiState.value.copy(pendingVenueSelection = null)
            return pending
        }
        return null
    }

    override suspend fun logout() {
        authTokenStore.clearSession()
        mutableUiState.value = ArenaSessionState(hasSeenOnboarding = authTokenStore.current().hasSeenOnboarding)
    }

    override suspend fun resetDemoData() = Unit
}
