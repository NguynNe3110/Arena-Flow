package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.InvitationCandidatePage
import com.igorwojda.showcase.feature.team.domain.repository.TeamMemberRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SearchInvitationCandidatesUseCaseTest {
    private val teamMemberRepository: TeamMemberRepository = mockk()
    private val sut = SearchInvitationCandidatesUseCase(teamMemberRepository)

    @Test
    fun `invoke searches invitation candidates`() =
        runTest {
            val expected = Result.Success(InvitationCandidatePage(items = emptyList(), hasMore = false))
            coEvery { teamMemberRepository.searchInvitationCandidates("team-1", "minh") } returns expected

            val actual = sut("team-1", "minh")

            assertEquals(expected, actual)
            coVerify(exactly = 1) { teamMemberRepository.searchInvitationCandidates("team-1", "minh") }
        }
}
