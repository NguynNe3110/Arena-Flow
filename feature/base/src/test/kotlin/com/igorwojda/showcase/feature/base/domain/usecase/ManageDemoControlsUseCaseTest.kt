package com.igorwojda.showcase.feature.base.domain.usecase

import com.igorwojda.showcase.feature.base.domain.model.PersonaRole
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ManageDemoControlsUseCaseTest {
    private val sessionRepository: SessionRepository = mockk(relaxed = true)
    private val sut = ManageDemoControlsUseCase(sessionRepository)

    @Test
    fun `when resetAll is true then calls resetDemoData`() =
        runTest {
            sut(resetAll = true)

            coVerify(exactly = 1) { sessionRepository.resetDemoData() }
        }

    @Test
    fun `when personaRole is set then calls switchPersona`() =
        runTest {
            sut(personaRole = PersonaRole.ADMIN)

            coVerify(exactly = 1) { sessionRepository.switchPersona(PersonaRole.ADMIN) }
        }
}
