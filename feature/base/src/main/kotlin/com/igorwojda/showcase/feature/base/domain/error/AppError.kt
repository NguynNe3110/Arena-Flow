package com.igorwojda.showcase.feature.base.domain.error

sealed interface AppError {
    data object Network : AppError

    data object Unauthorized : AppError

    data object Server : AppError

    data object NotFound : AppError

    data object Unknown : AppError

    data class DomainError(
        val httpStatus: Int,
        val code: String,
        val message: String,
        val fieldErrors: Map<String, String> = emptyMap(),
    ) : AppError
}

/** Resolves a Vietnamese user-facing message from any AppError. */
fun AppError.userMessage(fallback: String = "Đã xảy ra lỗi, vui lòng thử lại."): String =
    when (this) {
        is AppError.DomainError -> message.ifBlank { fallback }
        AppError.Network -> "Mất kết nối mạng. Vui lòng kiểm tra đường truyền và thử lại."
        AppError.Unauthorized -> "Phiên đăng nhập hết hạn hoặc bạn không có quyền thực hiện."
        AppError.NotFound -> "Nội dung không còn tồn tại hoặc đã bị xóa."
        AppError.Server -> "Hệ thống đang bận hoặc có xung đột dữ liệu, vui lòng thử lại."
        AppError.Unknown -> fallback
    }
