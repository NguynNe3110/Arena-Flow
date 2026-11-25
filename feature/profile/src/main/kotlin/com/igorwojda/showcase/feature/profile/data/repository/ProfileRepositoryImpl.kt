package com.igorwojda.showcase.feature.profile.data.repository

import com.igorwojda.showcase.feature.base.data.error.apiErrorToAppError
import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.data.error.toAppError
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.profile.data.datasource.api.model.UpdateProfileRequestDto
import com.igorwojda.showcase.feature.profile.data.datasource.api.model.toDomain
import com.igorwojda.showcase.feature.profile.data.datasource.api.service.ProfileRetrofitService
import com.igorwojda.showcase.feature.profile.domain.model.CaptainedTeam
import com.igorwojda.showcase.feature.profile.domain.model.Province
import com.igorwojda.showcase.feature.profile.domain.model.UpdateProfileParams
import com.igorwojda.showcase.feature.profile.domain.model.UserProfile
import com.igorwojda.showcase.feature.profile.domain.model.Ward
import com.igorwojda.showcase.feature.profile.domain.repository.ProfileRepository

internal class ProfileRepositoryImpl(
    private val service: ProfileRetrofitService,
) : ProfileRepository {
    override suspend fun getMyProfile(): Result<UserProfile> = executeApi { service.getMyProfile() }.map { it.data.toDomain() }

    override suspend fun updateMyProfile(params: UpdateProfileParams): Result<UserProfile> =
        executeApi {
            service.updateMyProfile(
                UpdateProfileRequestDto(
                    displayName = params.displayName,
                    avatarUrl = params.avatarUrl,
                    bio = params.bio,
                    preferredPosition = params.preferredPosition?.label,
                    dateOfBirth = params.dateOfBirth,
                    gender = params.gender?.apiValue,
                    wardId = params.wardId,
                ),
            )
        }.map { it.data.toDomain() }

    override suspend fun deleteAccount(): Result<Unit> = executeApi { service.deleteAccount() }

    override suspend fun getProvinces(): Result<List<Province>> =
        executeApi { service.getProvinces() }.map { envelope -> envelope.data.map { it.toDomain() } }

    override suspend fun getWardsByProvince(provinceId: String): Result<List<Ward>> =
        executeApi { service.getWardsByProvince(provinceId) }.map { envelope ->
            envelope.data.map { it.toDomain() }
        }

    override suspend fun getCaptainedTeams(): Result<List<CaptainedTeam>> =
        executeApi { service.getMyTeams(role = ROLE_CAPTAIN, page = 1, pageSize = MAX_PAGE_SIZE) }
            .map { envelope -> envelope.data.map { it.toDomain() } }

    private suspend fun <T> executeApi(call: suspend () -> ApiResult<T>): Result<T> =
        dataResult {
            when (val result = call()) {
                is ApiResult.Success -> {
                    Result.Success(result.data)
                }

                is ApiResult.Error -> {
                    Result.Failure(apiErrorToAppError(result.code, result.errorBody))
                }

                is ApiResult.Exception -> {
                    Result.Failure((result.throwable as? Exception)?.toAppError() ?: AppError.Unknown)
                }
            }
        }

    private inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> =
        when (this) {
            is Result.Success -> Result.Success(transform(value))
            is Result.Failure -> this
        }

    private companion object {
        const val ROLE_CAPTAIN = "CAPTAIN"
        const val MAX_PAGE_SIZE = 100
    }
}
