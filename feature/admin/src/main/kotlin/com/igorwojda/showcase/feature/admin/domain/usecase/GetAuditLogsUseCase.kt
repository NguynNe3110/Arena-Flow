package com.igorwojda.showcase.feature.admin.domain.usecase

import com.igorwojda.showcase.feature.admin.domain.model.AdminPage
import com.igorwojda.showcase.feature.admin.domain.model.AuditLogEntry
import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.domain.result.Result

private const val DEFAULT_PAGE_SIZE = 20

/** Loads paginated admin audit log entries. */
internal class GetAuditLogsUseCase(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(
        page: Int = 1,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): Result<AdminPage<AuditLogEntry>> = adminRepository.getAuditLogs(page = page, pageSize = pageSize)
}
