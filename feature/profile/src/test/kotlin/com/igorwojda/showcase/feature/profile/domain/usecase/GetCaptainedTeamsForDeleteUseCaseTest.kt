package com.igorwojda.showcase.feature.profile.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.profile.domain.model.CaptainedTeam
import com.igorwojda.showcase.feature.profile.domain.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetCaptainedTeamsForDeleteUseCaseTest {
    private val profileRepository: ProfileRepository = mockk()
    private val sut = GetCaptainedTeamsForDeleteUseCase(profileRepository)

    @Test
    fun `when invoked then returns captained teams from repository`() =
        runTest {
            val expected =
                Result.Success(
                    listOf(
                        CaptainedTeam(
                            teamId = "team-1",
                            teamName = "FC Sao Mai",
                            avatarUrl = null,
                            status = "ACTIVE",
                            memberCount = 5,
                            joinedAt = "2026-10-01T10:00:00",
                        ),
                    ),
                )
            coEvery { profileRepository.getCaptainedTeams() } returns expected

            val actual = sut()

            actual shouldBeEqualTo expected
        }
}
