package com.uzuu.team.data.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.team.data.datasource.api.TeamRemoteDataSource
import com.uzuu.team.domain.model.TeamDetail
import com.uzuu.team.domain.model.TeamDraft
import com.uzuu.team.domain.model.TeamMember
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TeamRepositoryImplTest {
    private val teamRemoteDataSource: TeamRemoteDataSource = mockk()

    private val sut = TeamRepositoryImpl(teamRemoteDataSource)

    @Test
    fun `getMyTeam returns success when datasource succeeds`() =
        runTest {
            val team = TeamDetail(id = "team-1", name = "Test FC", shortName = "TF")
            coEvery { teamRemoteDataSource.getMyTeam() } returns team

            val result = sut.getMyTeam()

            assertEquals(Result.Success(team), result)
        }

    @Test
    fun `createTeam returns success when datasource succeeds`() =
        runTest {
            val draft = TeamDraft(name = "New FC", shortName = "NF")
            val team = TeamDetail(id = "team-2", name = "New FC", shortName = "NF")
            coEvery { teamRemoteDataSource.createTeam(draft) } returns team

            val result = sut.createTeam(draft)

            assertEquals(Result.Success(team), result)
        }

    @Test
    fun `inviteTeamMember returns success when datasource succeeds`() =
        runTest {
            val member = TeamMember(id = "m-1", name = "Player", role = "Thành viên")
            coEvery { teamRemoteDataSource.inviteTeamMember("team-1", "Player", "Tiền đạo") } returns member

            val result = sut.inviteTeamMember("team-1", "Player", "Tiền đạo")

            assertEquals(Result.Success(member), result)
        }

    @Test
    fun `getMyTeam maps exception to failure`() =
        runTest {
            val exception = RuntimeException("Network error")
            coEvery { teamRemoteDataSource.getMyTeam() } throws exception

            val result = sut.getMyTeam()

            assertTrue(result is Result.Failure)
        }
}
