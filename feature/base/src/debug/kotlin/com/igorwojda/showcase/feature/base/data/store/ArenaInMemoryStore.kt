package com.igorwojda.showcase.feature.base.data.store

import com.igorwojda.showcase.feature.base.common.time.VietnamDateTime
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiErrorBodyDto
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiErrorEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.FieldValidationErrorDto
import com.igorwojda.showcase.feature.base.domain.model.PersonaRole
import com.igorwojda.showcase.feature.base.domain.model.SimulatedError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Debug-only reactive in-memory store synchronized across all Mock*RetrofitService implementations.
 * Never included in Release builds.
 */
class ArenaInMemoryStore {
    private val mutableSnapshot = MutableStateFlow(ArenaSeedFixtures.initialSnapshot())
    val snapshotFlow: StateFlow<ArenaStoreSnapshot> = mutableSnapshot.asStateFlow()
    private val json = Json { encodeDefaults = true }

    val current: ArenaStoreSnapshot
        get() = mutableSnapshot.value

    fun update(transform: (ArenaStoreSnapshot) -> ArenaStoreSnapshot) {
        mutableSnapshot.update(transform)
    }

    fun reset() {
        mutableSnapshot.value = ArenaSeedFixtures.initialSnapshot()
    }

    fun <T> apiError(
        httpStatus: Int,
        code: String,
        message: String,
        path: String = "/api/v1",
        validationErrors: List<FieldValidationErrorDto> = emptyList(),
    ): ApiResult.Error<T> {
        val envelope =
            ApiErrorEnvelope(
                error =
                    ApiErrorBodyDto(
                        code = code,
                        message = message,
                        timestamp = VietnamDateTime.nowIso(current.mockClockIso),
                        path = path,
                        validationErrors = validationErrors,
                    ),
            )
        val raw = json.encodeToString(ApiErrorEnvelope.serializer(), envelope)
        return ApiResult.Error(code = httpStatus, message = message, errorBody = raw)
    }

    /** Checks if a global simulated error is active, consumes it once, and emits a spec ApiResult failure. */
    fun <T> consumeSimulatedApiError(): ApiResult<T>? {
        val sim = current.simulatedError
        if (sim == SimulatedError.NONE) return null
        update { it.copy(simulatedError = SimulatedError.NONE) }
        return when (sim) {
            SimulatedError.FORBIDDEN_403 -> apiError(HTTP_FORBIDDEN, "AUTH_004", "Bạn không có quyền thực hiện thao tác này.")
            SimulatedError.NOT_FOUND_404 -> apiError(HTTP_NOT_FOUND, "SYS_001", "Không tìm thấy tài nguyên yêu cầu.")
            SimulatedError.CONFLICT_409 -> apiError(HTTP_CONFLICT, "PAIRING_006", "Phương án đã thay đổi, vui lòng tải lại.")
            SimulatedError.NETWORK -> ApiResult.Exception(IOException("Mô phỏng mất kết nối mạng"))
            SimulatedError.NONE -> null
        }
    }

    @Suppress("UnusedParameter")
    fun <T> executeApi(
        path: String = "/api/v1",
        block: (ArenaStoreSnapshot) -> ApiResult<T>,
    ): ApiResult<T> {
        val simulated = consumeSimulatedApiError<T>()
        if (simulated != null) return simulated
        return block(current)
    }

    fun wardLabel(wardId: String?): String {
        val ward = current.wards.firstOrNull { it.id == wardId }
        return ward?.fullLabel ?: "TP. Hồ Chí Minh"
    }

    fun displayUserName(
        userId: String?,
        viewerIsAdmin: Boolean = false,
    ): String {
        val user = current.users[userId] ?: return "Không rõ"
        if (user.isDeleted) {
            return if (viewerIsAdmin) {
                "${user.realName ?: "Ẩn danh"} (Đã xóa)"
            } else {
                "Người dùng đã xóa"
            }
        }
        return user.displayName.ifBlank { user.phone }
    }

    fun userIdForPersona(role: PersonaRole): String? =
        when (role) {
            PersonaRole.GUEST -> null
            PersonaRole.NEW_USER -> "user-new"
            PersonaRole.PLAYER -> "user-player"
            PersonaRole.CAPTAIN -> "user-cap-alpha"
            PersonaRole.MULTI_CAPTAIN -> "user-cap-multi"
            PersonaRole.MEMBER -> "user-member"
            PersonaRole.ADMIN -> "user-admin"
        }

    private companion object {
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
        const val HTTP_CONFLICT = 409
    }
}
