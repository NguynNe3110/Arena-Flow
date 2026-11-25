package com.igorwojda.showcase.feature.base.domain.usecase

import com.igorwojda.showcase.feature.base.domain.model.GpsPermissionState
import com.igorwojda.showcase.feature.base.domain.model.PersonaRole
import com.igorwojda.showcase.feature.base.domain.model.SimulatedError
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository

/** Applies demo control actions (persona switch, GPS, simulated error, mock clock, reset). */
class ManageDemoControlsUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(
        personaRole: PersonaRole? = null,
        gpsState: GpsPermissionState? = null,
        simulatedError: SimulatedError? = null,
        mockClockIso: String? = null,
        resetAll: Boolean = false,
    ) {
        if (resetAll) {
            sessionRepository.resetDemoData()
            return
        }
        if (personaRole != null) {
            sessionRepository.switchPersona(personaRole)
        }
        if (gpsState != null || simulatedError != null || mockClockIso != null) {
            sessionRepository.updateDemoControls(
                gpsState = gpsState,
                simulatedError = simulatedError,
                mockClockIso = mockClockIso,
            )
        }
    }
}
