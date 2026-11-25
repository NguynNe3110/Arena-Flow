package com.igorwojda.showcase.feature.admin.data.repository

import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdjustReputationRequestDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdminPendingVenueDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdminReportDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdminUserSummaryDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AuditLogDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.BanUserRequestDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.RejectVenueRequestDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.ResolveReportRequestDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.service.AdminModerationRetrofitService
import com.igorwojda.showcase.feature.admin.data.datasource.api.service.AdminUserRetrofitService
import com.igorwojda.showcase.feature.admin.domain.model.AdminReportStatus
import com.igorwojda.showcase.feature.admin.domain.model.ReportDecision
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginationDto
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AdminRepositoryImplTest {
    private val moderationService: AdminModerationRetrofitService = mockk()
    private val userService: AdminUserRetrofitService = mockk()
    private val sut = AdminRepositoryImpl(moderationService, userService)

    @Test
    fun `getReports maps paginated envelope to domain`() =
        runTest {
            val dto =
                AdminReportDto(
                    reportId = "rep-1",
                    reporterType = "TEAM",
                    reporterId = "t1",
                    reporterName = "FC A",
                    reportedId = "t2",
                    reportedName = "FC B",
                    type = "NOSHOW",
                    reason = "Bùng kèo",
                    status = "PENDING",
                    createdAt = "2026-11-18T10:00:00",
                )
            coEvery { moderationService.getReports(null, 1, 20) } returns
                ApiResult.Success(PaginatedEnvelope(listOf(dto), PaginationDto(1, 20, 1, 1)))

            val result = sut.getReports(null, 1, 20)

            assertTrue(result is Result.Success)
            val page = (result as Result.Success).value
            assertEquals(1, page.items.size)
            assertEquals(AdminReportStatus.PENDING, page.items.first().status)
        }

    @Test
    fun `resolveReport delegates and maps response`() =
        runTest {
            val dto =
                AdminReportDto(
                    reportId = "rep-1",
                    reporterType = "TEAM",
                    reporterId = "t1",
                    reporterName = "FC A",
                    reportedId = "t2",
                    reportedName = "FC B",
                    type = "NOSHOW",
                    reason = "Bùng kèo",
                    status = "CONFIRMED",
                    createdAt = "2026-11-18T10:00:00",
                    adminDecisionNote = "OK",
                )
            coEvery {
                moderationService.resolveReport("rep-1", ResolveReportRequestDto("CONFIRMED", "OK"))
            } returns ApiResult.Success(ApiEnvelope(dto))

            val result = sut.resolveReport("rep-1", ReportDecision.CONFIRM, "OK")

            assertTrue(result is Result.Success)
            assertEquals(AdminReportStatus.CONFIRMED, (result as Result.Success).value.status)
        }

    @Test
    fun `venue and user moderation methods delegate to services`() =
        runTest {
            val venueDto =
                AdminPendingVenueDto(
                    venueId = "v1",
                    venueName = "Sân 1",
                    address = "123 ABC",
                    wardLabel = "Q1",
                    venueType = "FIVE_A_SIDE",
                    createdById = "u1",
                    createdByName = "User 1",
                    createdAt = "2026-11-18T10:00:00",
                )
            val userDto =
                AdminUserSummaryDto(
                    userId = "u1",
                    displayName = "User 1",
                    phone = "0901234567",
                    role = "USER",
                    status = "ACTIVE",
                    reputationScore = 90,
                    matchCount = 5,
                )
            val logDto =
                AuditLogDto(
                    logId = "l1",
                    actorId = "admin",
                    actorName = "Admin",
                    action = "BAN_USER",
                    targetType = "USER",
                    targetId = "u1",
                    targetLabel = "User 1",
                    detail = "Spam",
                    createdAt = "2026-11-18T10:00:00",
                )
            coEvery { moderationService.getPendingVenues() } returns ApiResult.Success(ApiEnvelope(listOf(venueDto)))
            coEvery { moderationService.approveVenue("v1") } returns ApiResult.Success(Unit)
            coEvery { moderationService.rejectVenue("v1", RejectVenueRequestDto("Sai")) } returns ApiResult.Success(Unit)
            coEvery { userService.getUsers("User", null, 1, 20) } returns
                ApiResult.Success(PaginatedEnvelope(listOf(userDto), PaginationDto(1, 20, 1, 1)))
            coEvery { userService.banUser("u1", BanUserRequestDto("Spam")) } returns ApiResult.Success(Unit)
            coEvery { userService.unbanUser("u1") } returns ApiResult.Success(Unit)
            coEvery {
                userService.adjustReputation("u1", AdjustReputationRequestDto(-5, "Phạt"))
            } returns ApiResult.Success(Unit)
            coEvery { userService.getAuditLogs(1, 20) } returns
                ApiResult.Success(PaginatedEnvelope(listOf(logDto), PaginationDto(1, 20, 1, 1)))

            assertTrue(sut.getPendingVenues() is Result.Success)
            assertTrue(sut.approveVenue("v1") is Result.Success)
            assertTrue(sut.rejectVenue("v1", "Sai") is Result.Success)
            assertTrue(sut.getUsers("User", null, 1, 20) is Result.Success)
            assertTrue(sut.banUser("u1", "Spam") is Result.Success)
            assertTrue(sut.unbanUser("u1") is Result.Success)
            assertTrue(sut.adjustReputation("u1", -5, "Phạt") is Result.Success)
            assertTrue(sut.getAuditLogs(1, 20) is Result.Success)
            coVerify { moderationService.approveVenue("v1") }
            coVerify { userService.banUser("u1", BanUserRequestDto("Spam")) }
        }

    @Test
    fun `api error 403 maps to Unauthorized`() =
        runTest {
            coEvery { moderationService.getPendingVenues() } returns ApiResult.Error(403, "Forbidden", null)

            val result = sut.getPendingVenues()

            assertEquals(Result.Failure(AppError.Unauthorized), result)
        }
}
