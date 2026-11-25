package com.igorwojda.showcase.feature.mercenary.data.repository

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginationDto
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.MercenaryApplicationDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.MercenaryPostDetailDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.MercenaryPostSummaryDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.service.MercenaryCommandRetrofitService
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.service.MercenaryQueryRetrofitService
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostFilter
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MercenaryRepositoryImplTest {
    private val queryService: MercenaryQueryRetrofitService = mockk()
    private val commandService: MercenaryCommandRetrofitService = mockk()
    private val sut = MercenaryRepositoryImpl(queryService, commandService)

    @Test
    fun `getMercenaryPosts maps paginated dto to domain page`() =
        runTest {
            val dto =
                MercenaryPostSummaryDto(
                    id = "rec-1",
                    teamId = "team-alpha",
                    teamName = "FC Sao Mai",
                    position = "Thủ môn",
                    slotsNeeded = 1,
                    status = "OPEN",
                    matchStartAt = "2026-11-21T19:00:00",
                )
            coEvery {
                queryService.getMercenaryPosts(any(), any(), any(), any(), any(), any())
            } returns
                ApiResult.Success(
                    PaginatedEnvelope(
                        data = listOf(dto),
                        pagination = PaginationDto(page = 1, pageSize = 20, totalPages = 1, totalItems = 1),
                    ),
                )

            val result = sut.getMercenaryPosts(MercenaryPostFilter())

            assertTrue(result is Result.Success)
            val page = (result as Result.Success).value
            assertEquals(1, page.items.size)
            assertEquals("rec-1", page.items.first().id)
            assertEquals(PlayerPosition.GOALKEEPER, page.items.first().position)
        }

    @Test
    fun `getMercenaryPostDetail maps detail dto`() =
        runTest {
            val dto =
                MercenaryPostDetailDto(
                    id = "rec-1",
                    teamId = "team-alpha",
                    teamName = "FC Sao Mai",
                    position = "Tiền vệ",
                    slotsNeeded = 2,
                    status = "OPEN",
                    matchStartAt = "2026-11-21T19:00:00",
                )
            coEvery { queryService.getMercenaryPostDetail("rec-1") } returns ApiResult.Success(ApiEnvelope(dto))

            val result = sut.getMercenaryPostDetail("rec-1")

            assertTrue(result is Result.Success)
            assertEquals("FC Sao Mai", (result as Result.Success).value.teamName)
        }

    @Test
    fun `applyToMercenaryPost delegates to command service`() =
        runTest {
            val dto =
                MercenaryApplicationDto(
                    id = "app-1",
                    postId = "rec-1",
                    applicantId = "user-player",
                    position = "Thủ môn",
                    status = "PENDING",
                )
            coEvery { commandService.applyToMercenaryPost("rec-1", any()) } returns
                ApiResult.Success(ApiEnvelope(dto))

            val result = sut.applyToMercenaryPost("rec-1", PlayerPosition.GOALKEEPER, "Sẵn sàng")

            assertTrue(result is Result.Success)
            assertEquals("app-1", (result as Result.Success).value.id)
        }
}
