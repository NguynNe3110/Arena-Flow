@file:Suppress("ComplexInterface")

package com.igorwojda.showcase.feature.admin.domain.repository

import com.igorwojda.showcase.feature.admin.domain.model.AdminPage
import com.igorwojda.showcase.feature.admin.domain.model.AdminPendingVenue
import com.igorwojda.showcase.feature.admin.domain.model.AdminReport
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserSummary
import com.igorwojda.showcase.feature.admin.domain.model.AuditLogEntry
import com.igorwojda.showcase.feature.admin.domain.model.ReportDecision
import com.igorwojda.showcase.feature.base.domain.result.Result

/** Repository contract for ArenaFlow admin console operations. */
interface AdminRepository {
    suspend fun getReports(
        status: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<AdminPage<AdminReport>>

    suspend fun resolveReport(
        reportId: String,
        decision: ReportDecision,
        note: String?,
    ): Result<AdminReport>

    suspend fun getPendingVenues(): Result<List<AdminPendingVenue>>

    suspend fun approveVenue(venueId: String): Result<Unit>

    suspend fun rejectVenue(
        venueId: String,
        reason: String,
    ): Result<Unit>

    suspend fun getUsers(
        query: String? = null,
        status: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<AdminPage<AdminUserSummary>>

    suspend fun banUser(
        userId: String,
        reason: String,
    ): Result<Unit>

    suspend fun unbanUser(userId: String): Result<Unit>

    suspend fun adjustReputation(
        userId: String,
        delta: Int,
        reason: String,
    ): Result<Unit>

    suspend fun getAuditLogs(
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<AdminPage<AuditLogEntry>>
}
