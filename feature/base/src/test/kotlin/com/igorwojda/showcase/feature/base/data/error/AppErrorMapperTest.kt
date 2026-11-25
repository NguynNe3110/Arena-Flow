@file:Suppress("StringShouldBeRawString")

package com.igorwojda.showcase.feature.base.data.error

import com.igorwojda.showcase.feature.base.data.supabase.supabaseResult
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import io.github.jan.supabase.exceptions.RestException
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AppErrorMapperTest {
    @Test
    fun `Supabase HTTP failures share the domain classification`() =
        runBlocking {
            val exception = mockk<RestException>()
            every { exception.statusCode } returns 401
            assertEquals(Result.Failure(AppError.Unauthorized), supabaseResult<Int> { throw exception })
            every { exception.statusCode } returns 503
            assertEquals(Result.Failure(AppError.Server), supabaseResult<Int> { throw exception })
        }

    @Test
    fun `classifies HTTP and network failures`() {
        assertEquals(AppError.Unauthorized, httpError(401))
        assertEquals(AppError.Unauthorized, httpError(403))
        assertEquals(AppError.Server, httpError(503))
        assertEquals(AppError.Server, httpError(429))
        assertEquals(AppError.Unknown, httpError(400))
        assertEquals(AppError.Network, IOException().toAppError())
    }

    @Test
    fun `apiErrorToAppError parses ArenaFlow error envelope`() {
        val rawJson =
            "{" +
                "\"error\":{" +
                "\"code\":\"USER_003\"," +
                "\"message\":\"Bạn phải chuyển quyền đội trưởng hoặc giải tán đội trước khi xóa tài khoản\"," +
                "\"validationErrors\":[{\"field\":\"phone\",\"message\":\"Số điện thoại không hợp lệ\"}]" +
                "}" +
                "}"

        val actual = apiErrorToAppError(422, rawJson)
        assertTrue(actual is AppError.DomainError)
        val domainError = actual as AppError.DomainError
        assertEquals("USER_003", domainError.code)
        assertEquals(422, domainError.httpStatus)
        assertEquals("Số điện thoại không hợp lệ", domainError.fieldErrors["phone"])
    }

    @Test
    fun `data boundary returns classified errors`() =
        runBlocking {
            assertEquals(Result.Failure(AppError.Network), dataResult<Int> { throw IOException() })
            assertEquals(Result.Failure(AppError.Unknown), dataResult<Int> { throw IllegalStateException() })
        }

    @Test
    fun `cancellation propagates`() {
        val cancellation = CancellationException("cancelled")
        val actual =
            assertThrows(CancellationException::class.java) {
                runBlocking { dataResult<Int> { throw cancellation } }
            }
        assertEquals(cancellation, actual)
    }

    @Test
    fun `JVM errors propagate`() {
        assertThrows(AssertionError::class.java) {
            runBlocking { dataResult<Int> { throw AssertionError() } }
        }
    }
}
