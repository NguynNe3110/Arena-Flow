package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryApplication
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryApplicationStatus
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ApplyMercenaryUseCaseTest {
    private val mercenaryRepository: MercenaryRepository = mockk()
    private val sut = ApplyMercenaryUseCase(mercenaryRepository)

    @Test
    fun `invoke returns application from repository`() =
        runTest {
            val app =
                MercenaryApplication(
                    id = "app-1",
                    postId = "rec-1",
                    postTitle = "Tuyển Thủ môn",
                    teamName = "FC Sao Mai",
                    applicantId = "user-player",
                    applicantName = "Minh",
                    applicantReputationScore = 90,
                    applicantCompletionRate = 95,
                    position = PlayerPosition.GOALKEEPER,
                    note = "Sẵn sàng",
                    status = MercenaryApplicationStatus.PENDING,
                    conversationId = "conv-1",
                    matchStartAt = "2026-11-21T19:00:00",
                    areaLabel = "Thủ Đức",
                    allowanceVnd = 100_000,
                    createdAt = "2026-11-18T10:00:00",
                    approvedAt = null,
                )
            val expected = Result.Success(app)
            coEvery { mercenaryRepository.applyToMercenaryPost("rec-1", PlayerPosition.GOALKEEPER, "Sẵn sàng") } returns
                expected

            val actual = sut("rec-1", PlayerPosition.GOALKEEPER, "Sẵn sàng")

            assertEquals(expected, actual)
        }
}
