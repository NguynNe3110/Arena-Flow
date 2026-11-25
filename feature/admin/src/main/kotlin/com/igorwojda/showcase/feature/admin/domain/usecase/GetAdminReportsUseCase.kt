package com.igorwojda.showcase.feature.admin.domain.usecase

import com.igorwojda.showcase.feature.admin.domain.model.AdminPage
import com.igorwojda.showcase.feature.admin.domain.model.AdminReport
import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.domain.result.Result

private const val DEFAULT_PAGE_SIZE = 20

/** Loads paginated reports for the admin moderation queue. */
internal class GetAdminReportsUseCase(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(
        status: String? = null,
        page: Int = 1,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): Result<AdminPage<AdminReport>> = adminRepository.getReports(status = status, page = page, pageSize = pageSize)
}
