package com.uzuu.match.data.repository

import android.content.Context
import android.content.res.AssetManager
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.data.datasource.api.MatchRemoteDataSource
import com.uzuu.match.data.datasource.api.MockMatchDataSource
import com.uzuu.match.data.datasource.api.service.MatchApi
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.domain.usecase.GetMatchUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class MatchRepositoryImplTest {
    @Test
    fun `mock JSON passes through datasource repository and use case`() {
        val fixture = File(checkNotNull(System.getProperty("matchMockFixturePath"))).readText()
        val useCase = useCaseFor(mockApi(fixture))

        val result = runBlocking { useCase() }

        check(result is Result.Success)
        val page = result.value
        assertEquals(2, page.matches.size)
        assertEquals(1, page.page)
        assertEquals(20, page.pageSize)
        assertEquals(1, page.totalPages)
        assertEquals(2, page.totalItems)
        assertEquals(MatchType.HAS_VENUE, page.matches[0].type)
        assertEquals("Sân bóng An Phú", page.matches[0].venueName)
        assertEquals(MatchType.NEED_VENUE, page.matches[1].type)
        assertNull(page.matches[1].venueName)
        assertEquals(emptyList<String>(), page.matches[1].skillLevels)
    }

    @Test
    fun `malformed mock JSON becomes failure instead of escaping repository`() {
        val useCase = useCaseFor(mockApi("{"))

        val result = runBlocking { useCase() }

        assertEquals(Result.Failure(AppError.Unknown), result)
    }

    @Test
    fun `unauthorized API result becomes domain error`() {
        val api = mockk<MatchApi>()
        coEvery { api.getMatches() } returns ApiResult.Error(401, "Unauthorized")

        val result = runBlocking { useCaseFor(api)() }

        assertEquals(Result.Failure(AppError.Unauthorized), result)
    }

    @Test
    fun `cancellation is propagated instead of becoming a domain failure`() {
        val api = mockk<MatchApi>()
        coEvery { api.getMatches() } returns ApiResult.Exception(CancellationException("Cancelled"))

        assertThrows(CancellationException::class.java) {
            runBlocking { useCaseFor(api)() }
        }
    }

    @Test
    fun `detail fixtures contain venue options and the cost agreement without changing summaries`() = runBlocking {
        val list = File(checkNotNull(System.getProperty("matchMockFixturePath"))).readText()
        val details = File(checkNotNull(System.getProperty("matchDetailMockFixturePath"))).readText()
        val context = mockk<Context>()
        val assets = mockk<AssetManager>()
        every { context.assets } returns assets
        every { assets.open("mock/matches.json") } answers { list.byteInputStream() }
        every { assets.open("mock/match-details.json") } answers { details.byteInputStream() }
        val repository = MatchRepositoryImpl(MatchRemoteDataSource(MockMatchDataSource(context, Json { ignoreUnknownKeys = true })))
        val page = repository.getMatches() as Result.Success
        page.value.matches.forEach { summary ->
            val result = repository.getMatch(summary.matchId) as Result.Success
            assertEquals(summary.authorTeamName, result.value.summary.authorTeamName)
            assertEquals(summary.startsAt, result.value.summary.startsAt)
            assertEquals(summary.endsAt, result.value.summary.endsAt)
            if (summary.type == MatchType.HAS_VENUE) {
                assertEquals(600000, result.value.costAgreement.venueAmount)
                assertEquals("Tiền trọng tài", result.value.costAgreement.customItems.single().itemName)
            } else {
                assertEquals(2, result.value.venueOptions.size)
                assertEquals(2, result.value.timeOptions.size)
                assertNull(result.value.costAgreement.depositAmount)
            }
        }
        assertEquals(Result.Failure(AppError.NotFound), repository.getMatch("missing"))
    }

    @Test
    fun `createMatch creates a new match and adds it to repository`() = runBlocking {
        val list = File(checkNotNull(System.getProperty("matchMockFixturePath"))).readText()
        val details = File(checkNotNull(System.getProperty("matchDetailMockFixturePath"))).readText()
        val context = mockk<Context>()
        val assets = mockk<AssetManager>()
        every { context.assets } returns assets
        every { assets.open("mock/matches.json") } answers { list.byteInputStream() }
        every { assets.open("mock/match-details.json") } answers { details.byteInputStream() }
        val repository = MatchRepositoryImpl(MatchRemoteDataSource(MockMatchDataSource(context, Json { ignoreUnknownKeys = true })))

        val draft = com.uzuu.match.domain.model.MatchDraft(
            hostTeamName = "Test FC",
            venueName = "Test Stadium",
        )
        val created = repository.createMatch(draft)
        check(created is Result.Success)
        assertEquals("Test FC", created.value.authorTeamName)
        assertEquals(MatchType.HAS_VENUE, created.value.type)

        val page = repository.getMatches() as Result.Success
        assertEquals(3, page.value.matches.size)
        assertEquals("Test FC", page.value.matches[0].authorTeamName)
    }

    private fun mockApi(content: String): MatchApi {
        val context = mockk<Context>()
        val assets = mockk<AssetManager>()
        every { context.assets } returns assets
        every { assets.open("mock/matches.json") } answers { content.byteInputStream() }
        return MockMatchDataSource(context, Json { ignoreUnknownKeys = true })
    }

    private fun useCaseFor(api: MatchApi): GetMatchUseCase =
        GetMatchUseCase(MatchRepositoryImpl(MatchRemoteDataSource(api)))
}
