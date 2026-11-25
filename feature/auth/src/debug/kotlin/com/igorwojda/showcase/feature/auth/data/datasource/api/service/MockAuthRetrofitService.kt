@file:Suppress("LabeledExpression")

package com.igorwojda.showcase.feature.auth.data.datasource.api.service

import com.igorwojda.showcase.feature.auth.data.datasource.api.model.AuthResponseDto
import com.igorwojda.showcase.feature.auth.data.datasource.api.model.OAuthRequestDto
import com.igorwojda.showcase.feature.auth.data.datasource.api.model.RefreshRequestDto
import com.igorwojda.showcase.feature.auth.data.datasource.api.model.SendOtpRequestDto
import com.igorwojda.showcase.feature.auth.data.datasource.api.model.SendOtpResponseDto
import com.igorwojda.showcase.feature.auth.data.datasource.api.model.VerifyOtpRequestDto
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.FieldValidationErrorDto
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.StoreUser
import java.util.UUID

/** Debug-only [AuthRetrofitService] backed by [ArenaInMemoryStore]. */
@Suppress("MagicNumber")
internal class MockAuthRetrofitService(
    private val store: ArenaInMemoryStore,
) : AuthRetrofitService {
    override suspend fun signInWithOAuth(request: OAuthRequestDto): ApiResult<ApiEnvelope<AuthResponseDto>> =
        store.executeApi(path = PATH_OAUTH) { snapshot ->
            val normalizedEmail = request.email.trim().lowercase()
            if (request.oauthToken.isBlank() || normalizedEmail.isBlank()) {
                return@executeApi validationError(PATH_OAUTH, "email", "Email và token không được để trống.")
            }
            if (normalizedEmail == DELETED_EMAIL || snapshot.isDeletedAccountBlocked) {
                return@executeApi deletedAccountError(PATH_OAUTH)
            }
            val existing = snapshot.users.values.firstOrNull { it.email?.equals(normalizedEmail, ignoreCase = true) == true }
            if (existing?.isDeleted == true) {
                return@executeApi deletedAccountError(PATH_OAUTH)
            }
            val user = existing ?: createNewOAuthUser(normalizedEmail)
            activateUser(user, existing == null)
        }

    override suspend fun sendOtp(request: SendOtpRequestDto): ApiResult<ApiEnvelope<SendOtpResponseDto>> =
        store.executeApi(path = PATH_SEND_OTP) {
            if (!PHONE_REGEX.matches(request.phone)) {
                return@executeApi validationError(PATH_SEND_OTP, "phone", "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.")
            }
            ApiResult.Success(ApiEnvelope(SendOtpResponseDto(phone = request.phone, expiresIn = OTP_EXPIRES_SECONDS)))
        }

    override suspend fun verifyOtp(request: VerifyOtpRequestDto): ApiResult<ApiEnvelope<AuthResponseDto>> =
        store.executeApi(path = PATH_VERIFY_OTP) { snapshot ->
            if (!PHONE_REGEX.matches(request.phone)) {
                return@executeApi validationError(PATH_VERIFY_OTP, "phone", "Số điện thoại không hợp lệ.")
            }
            if (!OTP_REGEX.matches(request.otp)) {
                return@executeApi validationError(PATH_VERIFY_OTP, "otp", "Mã OTP phải gồm 6 chữ số.")
            }
            if (request.phone == DELETED_PHONE || snapshot.isDeletedAccountBlocked) {
                return@executeApi deletedAccountError(PATH_VERIFY_OTP)
            }
            val existing = snapshot.users.values.firstOrNull { it.phone == request.phone }
            if (existing?.isDeleted == true) {
                return@executeApi deletedAccountError(PATH_VERIFY_OTP)
            }
            val user = existing ?: createNewPhoneUser(request.phone)
            activateUser(user, existing == null)
        }

    override suspend fun refreshToken(request: RefreshRequestDto): ApiResult<ApiEnvelope<AuthResponseDto>> =
        store.executeApi(path = PATH_REFRESH) { snapshot ->
            if (request.refreshToken.isBlank()) {
                return@executeApi store.apiError(401, "AUTH_002", "Refresh token không hợp lệ.", path = PATH_REFRESH)
            }
            val user = snapshot.users[snapshot.currentUserId] ?: snapshot.users["user-cap-alpha"]
            if (user == null || user.isDeleted || snapshot.isDeletedAccountBlocked) {
                return@executeApi deletedAccountError(PATH_REFRESH)
            }
            ApiResult.Success(ApiEnvelope(buildAuthResponse(user, isNewUser = false)))
        }

    private fun activateUser(
        user: StoreUser,
        isNewRecord: Boolean,
    ): ApiResult<ApiEnvelope<AuthResponseDto>> {
        store.update { state ->
            val updatedUsers = if (isNewRecord) state.users + (user.id to user) else state.users
            state.copy(
                users = updatedUsers,
                currentUserId = user.id,
                isDeletedAccountBlocked = false,
            )
        }
        return ApiResult.Success(ApiEnvelope(buildAuthResponse(user, isNewUser = user.isNewUser)))
    }

    private fun createNewOAuthUser(email: String): StoreUser =
        StoreUser(
            id = "user-${UUID.randomUUID()}",
            phone = "",
            email = email,
            displayName = "",
            isNewUser = true,
        )

    private fun createNewPhoneUser(phone: String): StoreUser =
        StoreUser(
            id = "user-${UUID.randomUUID()}",
            phone = phone,
            displayName = "",
            isNewUser = true,
        )

    private fun buildAuthResponse(
        user: StoreUser,
        isNewUser: Boolean,
    ): AuthResponseDto =
        AuthResponseDto(
            userId = user.id,
            role = user.role.name,
            isNewUser = isNewUser,
            accessToken = "mock-access-${user.id}",
            refreshToken = "mock-refresh-${user.id}",
            tokenType = "Bearer",
            expiresIn = ACCESS_TOKEN_EXPIRES_SECONDS,
        )

    private fun deletedAccountError(path: String): ApiResult.Error<ApiEnvelope<AuthResponseDto>> =
        store.apiError(
            httpStatus = 403,
            code = "AUTH_003",
            message = "Tài khoản này đã bị xóa vĩnh viễn và không thể khôi phục.",
            path = path,
        )

    private fun <T> validationError(
        path: String,
        field: String,
        message: String,
    ): ApiResult.Error<T> =
        store.apiError(
            httpStatus = 400,
            code = "VALIDATION_ERROR",
            message = message,
            path = path,
            validationErrors = listOf(FieldValidationErrorDto(field = field, message = message)),
        )

    private companion object {
        const val PATH_OAUTH = "/api/v1/auth/oauth"
        const val PATH_SEND_OTP = "/api/v1/auth/send-otp"
        const val PATH_VERIFY_OTP = "/api/v1/auth/verify-otp"
        const val PATH_REFRESH = "/api/v1/auth/refresh"
        const val DELETED_PHONE = "0900000099"
        const val DELETED_EMAIL = "deleted@arenaflow.vn"
        const val OTP_EXPIRES_SECONDS = 300
        const val ACCESS_TOKEN_EXPIRES_SECONDS = 3600
        val PHONE_REGEX = Regex("^0\\d{9}$")
        val OTP_REGEX = Regex("^\\d{6}$")
    }
}
