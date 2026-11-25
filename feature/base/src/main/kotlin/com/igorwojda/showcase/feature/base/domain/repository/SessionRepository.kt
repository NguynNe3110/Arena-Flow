@file:Suppress("ComplexInterface")

package com.igorwojda.showcase.feature.base.domain.repository

import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.model.GpsPermissionState
import com.igorwojda.showcase.feature.base.domain.model.PersonaRole
import com.igorwojda.showcase.feature.base.domain.model.SelectedVenueResult
import com.igorwojda.showcase.feature.base.domain.model.SimulatedError
import com.igorwojda.showcase.feature.base.domain.model.WardOption
import kotlinx.coroutines.flow.Flow

/** Shared reactive session, acting-team, venue-picker result, and demo control repository. */
interface SessionRepository {
    fun observeSession(): Flow<ArenaSessionState>

    fun getWards(): List<WardOption>

    suspend fun completeOnboarding()

    suspend fun switchPersona(role: PersonaRole)

    suspend fun setActingTeam(teamId: String)

    suspend fun updateDemoControls(
        gpsState: GpsPermissionState? = null,
        simulatedError: SimulatedError? = null,
        mockClockIso: String? = null,
    )

    suspend fun setPendingVenueSelection(result: SelectedVenueResult)

    suspend fun consumePendingVenueSelection(requestKey: String): SelectedVenueResult?

    suspend fun logout()

    suspend fun resetDemoData()
}
