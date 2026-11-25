package com.igorwojda.showcase.feature.base.domain.usecase

import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetSessionStateUseCaseTest {
    private val sessionRepository: SessionRepository = mockk()
    private val sut = GetSessionStateUseCase(sessionRepository)

    @Test
    fun `when invoked then returns session flow from repository`() =
        runTest {
            val expected = ArenaSessionState(hasSeenOnboarding = true)
            every { sessionRepository.observeSession() } returns flowOf(expected)

            val actual = sut().first()

            actual shouldBeEqualTo expected
        }
}
