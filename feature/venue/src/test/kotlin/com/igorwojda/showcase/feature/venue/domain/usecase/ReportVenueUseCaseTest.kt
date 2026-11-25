package com.igorwojda.showcase.feature.venue.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.domain.model.VenueReport
import com.igorwojda.showcase.feature.venue.domain.model.VenueReportReason
import com.igorwojda.showcase.feature.venue.domain.repository.VenueRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ReportVenueUseCaseTest {
    private val venueRepository: VenueRepository = mockk()
    private val sut = ReportVenueUseCase(venueRepository)

    @Test
    fun `when invoked then delegates reportVenue to repository`() =
        runTest {
            val expected =
                Result.Success(
                    VenueReport(
                        reportId = "rep-1",
                        venueId = "ven-1",
                        reason = VenueReportReason.CLOSED,
                        detail = "Đã đóng cửa",
                        status = "PENDING",
                        createdAt = "2026-11-18T10:00:00",
                    ),
                )
            coEvery { venueRepository.reportVenue("ven-1", VenueReportReason.CLOSED, "Đã đóng cửa") } returns expected

            val actual = sut("ven-1", VenueReportReason.CLOSED, "Đã đóng cửa")

            actual shouldBeEqualTo expected
            coVerify(exactly = 1) { venueRepository.reportVenue("ven-1", VenueReportReason.CLOSED, "Đã đóng cửa") }
        }
}
