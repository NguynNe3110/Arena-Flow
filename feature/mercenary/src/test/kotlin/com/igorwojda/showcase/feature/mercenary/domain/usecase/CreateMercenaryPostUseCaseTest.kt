package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.model.SkillLevel
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.model.CreateMercenaryPostParams
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CreateMercenaryPostUseCaseTest {
    private val mercenaryRepository: MercenaryRepository = mockk()
    private val sut = CreateMercenaryPostUseCase(mercenaryRepository)

    @Test
    fun `invoke delegates creation to repository`() =
        runTest {
            val params =
                CreateMercenaryPostParams(
                    teamId = "team-alpha",
                    venueId = "ven-1",
                    format = PitchFormat.SEVEN_A_SIDE,
                    neededPositions = listOf(PlayerPosition.GOALKEEPER),
                    slotsNeeded = 1,
                    allowanceVnd = 100_000,
                    matchStartAt = "2026-11-23T19:00:00",
                    skillLevel = SkillLevel.INTERMEDIATE,
                    notes = "",
                )
            val expected = Result.Failure(AppError.Network)
            coEvery { mercenaryRepository.createMercenaryPost(params) } returns expected

            val actual = sut(params)

            assertEquals(expected, actual)
        }
}
