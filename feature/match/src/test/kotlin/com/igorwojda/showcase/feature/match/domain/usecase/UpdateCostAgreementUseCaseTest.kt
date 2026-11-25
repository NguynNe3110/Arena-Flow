package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.MatchCostAgreement
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class UpdateCostAgreementUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = UpdateCostAgreementUseCase(matchRepository)

    @Test
    fun `when invoked then delegates to repository`() =
        runTest {
            val agreement = MatchCostAgreement(costType = "VENUE_ONLY", costSplit = "FIFTY_FIFTY", venueAmount = 600_000)
            val expected = Result.Success(agreement)
            coEvery { matchRepository.updateCostAgreement("match-1", agreement) } returns expected

            val actual = sut("match-1", agreement)

            actual shouldBeEqualTo expected
        }
}
