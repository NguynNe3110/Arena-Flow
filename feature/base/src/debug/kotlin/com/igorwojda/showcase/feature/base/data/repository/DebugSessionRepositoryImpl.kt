@file:Suppress("TooManyFunctions")

package com.igorwojda.showcase.feature.base.data.repository

import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.ArenaStoreSnapshot
import com.igorwojda.showcase.feature.base.domain.model.ActingTeamOption
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
import kotlinx.coroutines.flow.map

/** Debug-only SessionRepository backed by ArenaInMemoryStore & Demo Control Panel. */
internal class DebugSessionRepositoryImpl(
    private val authTokenStore: AuthTokenStore,
    private val store: ArenaInMemoryStore,
) : SessionRepository {
    override fun observeSession(): Flow<ArenaSessionState> = store.snapshotFlow.map { snapshot -> snapshot.toSessionState() }

    override fun getWards(): List<WardOption> = store.current.wards

    override suspend fun completeOnboarding() {
        authTokenStore.setHasSeenOnboarding(true)
        store.update { it.copy(hasSeenOnboarding = true) }
    }

    override suspend fun switchPersona(role: PersonaRole) {
        val targetUserId = store.userIdForPersona(role)
        store.update { snapshot ->
            val firstCaptainedTeam =
                targetUserId?.let { uid ->
                    snapshot.teams.firstOrNull { it.captainId == uid && it.status == "ACTIVE" }?.id
                }
            snapshot.copy(
                currentUserId = targetUserId,
                actingTeamId = firstCaptainedTeam,
                isDeletedAccountBlocked = false,
            )
        }
    }

    override suspend fun setActingTeam(teamId: String) {
        store.update { it.copy(actingTeamId = teamId) }
    }

    override suspend fun updateDemoControls(
        gpsState: GpsPermissionState?,
        simulatedError: SimulatedError?,
        mockClockIso: String?,
    ) {
        store.update { snapshot ->
            snapshot.copy(
                gpsState = gpsState ?: snapshot.gpsState,
                simulatedError = simulatedError ?: snapshot.simulatedError,
                mockClockIso = mockClockIso ?: snapshot.mockClockIso,
            )
        }
    }

    override suspend fun setPendingVenueSelection(result: SelectedVenueResult) {
        store.update { it.copy(pendingVenueSelection = result) }
    }

    override suspend fun consumePendingVenueSelection(requestKey: String): SelectedVenueResult? {
        val pending = store.current.pendingVenueSelection
        if (pending != null && pending.requestKey == requestKey) {
            store.update { it.copy(pendingVenueSelection = null) }
            return pending
        }
        return null
    }

    override suspend fun logout() {
        authTokenStore.clearSession()
        store.update { it.copy(currentUserId = null, actingTeamId = null, isDeletedAccountBlocked = false) }
    }

    override suspend fun resetDemoData() {
        store.reset()
    }

    private fun ArenaStoreSnapshot.toSessionState(): ArenaSessionState {
        val user = currentUserId?.let { users[it] }
        val userSummary =
            user?.let {
                SessionUserSummary(
                    id = it.id,
                    phone = it.phone,
                    displayName = it.displayName,
                    preferredPosition = it.preferredPosition,
                    wardId = it.wardId,
                    role = it.role,
                    isNewUser = it.isNewUser,
                    reputationScore = it.reputationScore,
                    matchCount = it.matchCount,
                )
            }
        val captained =
            if (user == null) {
                emptyList()
            } else {
                teams
                    .filter { it.captainId == user.id && it.status == "ACTIVE" }
                    .map { team ->
                        ActingTeamOption(
                            id = team.id,
                            name = team.name,
                            wardName = store.wardLabel(team.wardId),
                            reputationScore = team.reputationScore,
                            status = team.status,
                        )
                    }
            }
        val uid = user?.id
        val unreadNotifs = if (uid == null) 0 else notifications.count { it.userId == uid && !it.read }
        val unreadMsgs =
            if (uid == null) {
                0
            } else {
                conversations
                    .filter { uid in it.participantIds }
                    .sumOf { conv -> conv.messages.count { uid !in it.readBy } }
            }

        return ArenaSessionState(
            hasSeenOnboarding = hasSeenOnboarding,
            currentUser = userSummary,
            actingTeamId = actingTeamId ?: captained.firstOrNull()?.id,
            captainedTeams = captained,
            unreadNotifications = unreadNotifs,
            unreadMessages = unreadMsgs,
            mockClockIso = mockClockIso,
            gpsState = gpsState,
            simulatedError = simulatedError,
            pendingVenueSelection = pendingVenueSelection,
            isDeletedAccountBlocked = isDeletedAccountBlocked,
        )
    }
}
