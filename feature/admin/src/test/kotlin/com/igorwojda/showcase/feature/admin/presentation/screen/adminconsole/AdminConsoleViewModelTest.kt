package com.igorwojda.showcase.feature.admin.presentation.screen.adminconsole

import com.igorwojda.showcase.feature.admin.domain.model.AdminPage
import com.igorwojda.showcase.feature.admin.domain.model.AdminPendingVenue
import com.igorwojda.showcase.feature.admin.domain.model.AdminReport
import com.igorwojda.showcase.feature.admin.domain.model.AdminReportStatus
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserStatus
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserSummary
import com.igorwojda.showcase.feature.admin.domain.model.AuditLogEntry
import com.igorwojda.showcase.feature.admin.domain.model.ReportDecision
import com.igorwojda.showcase.feature.admin.domain.usecase.AdjustUserReputationUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.ApproveVenueUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.BanUserUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.GetAdminReportsUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.GetAdminUsersUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.GetAuditLogsUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.GetPendingVenuesUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.RejectVenueUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.ResolveAdminReportUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.UnbanUserUseCase
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class AdminConsoleViewModelTest {
    private val adjustUserReputationUseCase: AdjustUserReputationUseCase = mockk()
    private val approveVenueUseCase: ApproveVenueUseCase = mockk()
    private val banUserUseCase: BanUserUseCase = mockk()
    private val getAdminReportsUseCase: GetAdminReportsUseCase = mockk()
    private val getAdminUsersUseCase: GetAdminUsersUseCase = mockk()
    private val getAuditLogsUseCase: GetAuditLogsUseCase = mockk()
    private val getPendingVenuesUseCase: GetPendingVenuesUseCase = mockk()
    private val rejectVenueUseCase: RejectVenueUseCase = mockk()
    private val resolveAdminReportUseCase: ResolveAdminReportUseCase = mockk()
    private val unbanUserUseCase: UnbanUserUseCase = mockk()

    private lateinit var sut: AdminConsoleViewModel

    private fun createSut(): AdminConsoleViewModel =
        AdminConsoleViewModel(
            adjustUserReputationUseCase = adjustUserReputationUseCase,
            approveVenueUseCase = approveVenueUseCase,
            banUserUseCase = banUserUseCase,
            getAdminReportsUseCase = getAdminReportsUseCase,
            getAdminUsersUseCase = getAdminUsersUseCase,
            getAuditLogsUseCase = getAuditLogsUseCase,
            getPendingVenuesUseCase = getPendingVenuesUseCase,
            rejectVenueUseCase = rejectVenueUseCase,
            resolveAdminReportUseCase = resolveAdminReportUseCase,
            unbanUserUseCase = unbanUserUseCase,
        )

    @Test
    fun `init loads reports queue successfully`() =
        runTest {
            val report = sampleReport()
            coEvery { getAdminReportsUseCase(status = null) } returns Result.Success(samplePage(listOf(report)))

            sut = createSut()
            advanceUntilIdle()

            assertEquals(AdminConsoleTab.REPORTS, sut.uiStateFlow.value.selectedTab)
            assertEquals(listOf(report), sut.uiStateFlow.value.reports)
            assertFalse(sut.uiStateFlow.value.isLoading)
            assertNull(sut.uiStateFlow.value.error)
        }

    @Test
    fun `selecting venues tab loads pending venues`() =
        runTest {
            val venue = sampleVenue()
            coEvery { getAdminReportsUseCase(status = null) } returns Result.Success(samplePage(emptyList()))
            coEvery { getPendingVenuesUseCase() } returns Result.Success(listOf(venue))

            sut = createSut()
            advanceUntilIdle()
            sut.onEvent(AdminConsoleEvent.SelectTab(AdminConsoleTab.VENUES))
            advanceUntilIdle()

            assertEquals(AdminConsoleTab.VENUES, sut.uiStateFlow.value.selectedTab)
            assertEquals(listOf(venue), sut.uiStateFlow.value.pendingVenues)
        }

    @Test
    fun `selecting users and audit logs tabs loads corresponding data`() =
        runTest {
            val user = sampleUser()
            val log = sampleAuditLog()
            coEvery { getAdminReportsUseCase(status = null) } returns Result.Success(samplePage(emptyList()))
            coEvery { getAdminUsersUseCase(query = null, status = null) } returns Result.Success(samplePage(listOf(user)))
            coEvery { getAuditLogsUseCase() } returns Result.Success(samplePage(listOf(log)))

            sut = createSut()
            advanceUntilIdle()
            sut.onEvent(AdminConsoleEvent.SelectTab(AdminConsoleTab.USERS))
            advanceUntilIdle()
            assertEquals(listOf(user), sut.uiStateFlow.value.users)

            sut.onEvent(AdminConsoleEvent.SelectTab(AdminConsoleTab.AUDIT_LOGS))
            advanceUntilIdle()
            assertEquals(listOf(log), sut.uiStateFlow.value.auditLogs)
        }

    @Test
    fun `submit report decision resolves report and reloads list`() =
        runTest {
            val report = sampleReport()
            val resolved = report.copy(status = AdminReportStatus.CONFIRMED, adminDecisionNote = "Đã xác nhận")
            coEvery { getAdminReportsUseCase(status = null) } returnsMany
                listOf(
                    Result.Success(samplePage(listOf(report))),
                    Result.Success(samplePage(listOf(resolved))),
                )
            coEvery {
                resolveAdminReportUseCase("rep-1", ReportDecision.CONFIRM, "Đã xác nhận")
            } returns Result.Success(resolved)

            sut = createSut()
            advanceUntilIdle()
            sut.onEvent(AdminConsoleEvent.OpenResolveReportDialog(report))
            sut.onEvent(AdminConsoleEvent.SubmitReportDecision("rep-1", ReportDecision.CONFIRM, "Đã xác nhận"))
            advanceUntilIdle()

            coVerify { resolveAdminReportUseCase("rep-1", ReportDecision.CONFIRM, "Đã xác nhận") }
            assertNull(sut.uiStateFlow.value.activeDialog)
            assertEquals("Đã xử lý báo cáo vi phạm.", sut.uiStateFlow.value.feedbackMessage)
            assertEquals(
                AdminReportStatus.CONFIRMED,
                sut.uiStateFlow.value.reports
                    .first()
                    .status,
            )
        }

    @Test
    fun `non admin error sets Unauthorized state`() =
        runTest {
            coEvery { getAdminReportsUseCase(status = null) } returns Result.Failure(AppError.Unauthorized)

            sut = createSut()
            advanceUntilIdle()

            assertEquals(AppError.Unauthorized, sut.uiStateFlow.value.error)
        }

    private fun <T> samplePage(items: List<T>) = AdminPage(items = items, page = 1, pageSize = 20, totalPages = 1, totalItems = items.size)

    private fun sampleReport() =
        AdminReport(
            reportId = "rep-1",
            reporterType = "TEAM",
            reporterId = "team-a",
            reporterName = "FC Sao Mai",
            isReporterDeleted = false,
            reportedId = "team-b",
            reportedName = "Sài Gòn United",
            isReportedDeleted = false,
            matchId = "match-1",
            type = "NOSHOW",
            reason = "Không đến sân",
            status = AdminReportStatus.PENDING,
            createdAt = "2026-11-18T10:00:00",
            canDisputeUntil = null,
            disputeNote = null,
            adminDecisionNote = null,
        )

    private fun sampleVenue() =
        AdminPendingVenue(
            venueId = "ven-1",
            venueName = "Sân K34",
            address = "24 Cộng Hòa",
            wardLabel = "Tân Bình",
            venueType = "SEVEN_A_SIDE",
            createdById = "u1",
            createdByName = "Hùng",
            isCreatedByDeleted = false,
            createdAt = "2026-11-18T10:00:00",
            contactPhone = "0904000004",
        )

    private fun sampleUser() =
        AdminUserSummary(
            userId = "u1",
            displayName = "Trần Văn Minh",
            realName = null,
            phone = "0902000002",
            role = "USER",
            status = AdminUserStatus.ACTIVE,
            reputationScore = 95,
            matchCount = 14,
            isDeleted = false,
            banReason = null,
        )

    private fun sampleAuditLog() =
        AuditLogEntry(
            logId = "log-1",
            actorId = "admin-1",
            actorName = "Admin",
            action = "APPROVE_VENUE",
            targetType = "VENUE",
            targetId = "ven-1",
            targetLabel = "Sân K34",
            detail = "Phê duyệt sân",
            createdAt = "2026-11-18T11:00:00",
        )
}
