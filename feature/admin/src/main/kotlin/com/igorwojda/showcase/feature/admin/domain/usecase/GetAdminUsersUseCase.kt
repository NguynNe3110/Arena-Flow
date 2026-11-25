package com.igorwojda.showcase.feature.admin.domain.usecase

import com.igorwojda.showcase.feature.admin.domain.model.AdminPage
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserSummary
import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.domain.result.Result

private const val DEFAULT_PAGE_SIZE = 20

/** Searches and lists users for admin moderation. */
internal class GetAdminUsersUseCase(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(
        query: String? = null,
        status: String? = null,
        page: Int = 1,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): Result<AdminPage<AdminUserSummary>> = adminRepository.getUsers(query = query, status = status, page = page, pageSize = pageSize)
}
