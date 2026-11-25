@file:Suppress("TooGenericExceptionCaught")

package com.igorwojda.showcase.feature.base.data.error

import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiErrorEnvelope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import io.github.jan.supabase.exceptions.RestException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

private val errorJson = Json { ignoreUnknownKeys = true }

@Suppress("detekt.MagicNumber")
fun httpError(code: Int): AppError =
    when (code) {
        401, 403 -> AppError.Unauthorized
        404 -> AppError.NotFound
        in 500..599, 429 -> AppError.Server
        else -> AppError.Unknown
    }

/** Parses ArenaFlowSpec `ApiErrorEnvelope` when available, otherwise falls back to `httpError`. */
fun apiErrorToAppError(
    code: Int,
    errorBody: String?,
): AppError {
    if (!errorBody.isNullOrBlank()) {
        val parsed = runCatching { errorJson.decodeFromString<ApiErrorEnvelope>(errorBody) }.getOrNull()
        if (parsed != null) {
            val body = parsed.error
            return AppError.DomainError(
                httpStatus = code,
                code = body.code,
                message = body.message,
                fieldErrors = body.validationErrors.associate { it.field to it.message },
            )
        }
    }
    return httpError(code)
}

fun Exception.toAppError(): AppError =
    when (this) {
        is CancellationException -> throw this
        is RestException -> httpError(statusCode)
        is HttpException -> httpError(code())
        is IOException -> AppError.Network
        else -> AppError.Unknown
    }

/** Data boundary only; cancellation and JVM Errors propagate. */
suspend fun <T> dataResult(block: suspend () -> Result<T>): Result<T> =
    try {
        block()
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.Failure(exception.toAppError())
    }
