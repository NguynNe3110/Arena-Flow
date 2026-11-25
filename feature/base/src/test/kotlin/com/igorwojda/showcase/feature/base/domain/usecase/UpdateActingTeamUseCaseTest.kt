package com.igorwojda.showcase.feature.base.domain.usecase

import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateActingTeamUseCaseTest {
    private val sessionRepository: SessionRepository = mockk(relaxed = true)
    private val sut = UpdateActingTeamUseCase(sessionRepository)

    @Test
    fun `when invoked then delegates teamId to repository`() =
        runTest {
            sut("team-multi-2")

            coVerify(exactly = 1) { sessionRepository.setActingTeam("team-multi-2") }
        }
}
