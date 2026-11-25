package com.igorwojda.showcase.feature.venue.domain.usecase

import com.igorwojda.showcase.feature.base.domain.model.SelectedVenueResult
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SelectVenueForCallerUseCaseTest {
    private val sessionRepository: SessionRepository = mockk()
    private val sut = SelectVenueForCallerUseCase(sessionRepository)

    @Test
    fun `when invoked then sets pending venue selection in session repository`() =
        runTest {
            val selection =
                SelectedVenueResult(
                    requestKey = "match-create",
                    venueId = "ven-anphu",
                    venueName = "Sân bóng An Phú",
                    venueAddress = "Số 8 Đường số 2",
                )
            coEvery { sessionRepository.setPendingVenueSelection(selection) } returns Unit

            sut(selection)

            coVerify(exactly = 1) { sessionRepository.setPendingVenueSelection(selection) }
        }
}
