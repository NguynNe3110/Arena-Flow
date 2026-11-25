package com.igorwojda.showcase.feature.pairing.data.repository

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.pairing.PairingTestFixtures
import com.igorwojda.showcase.feature.pairing.data.datasource.api.service.PairingRetrofitService
import com.igorwojda.showcase.feature.pairing.domain.model.PairingStatus
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import java.io.IOException

class PairingRepositoryImplTest {
    private val service: PairingRetrofitService = mockk()
    private val sut = PairingRepositoryImpl(service)

    @Test
    fun `when getPairingRoom succeeds then returns mapped domain room`() =
        runTest {
            coEvery { service.getPairingRoom("pairing-beta-alpha") } returns
                ApiResult.Success(ApiEnvelope(PairingTestFixtures.sampleRoomDto()))

            val result = sut.getPairingRoom("pairing-beta-alpha")

            result.shouldBeInstanceOf<Result.Success<*>>()
            val room = (result as Result.Success).value
            room.roomId shouldBeEqualTo "pairing-beta-alpha"
            room.status shouldBeEqualTo PairingStatus.NEGOTIATING
        }

    @Test
    fun `when confirmPairing returns 409 error envelope then maps to DomainError`() =
        runTest {
            val errorBody =
                """{"error":{"code":"PAIRING_006","message":"Phương án đã thay đổi, vui lòng tải lại."}}"""
            coEvery { service.confirmPairing("pairing-beta-alpha", any()) } returns
                ApiResult.Error(code = 409, message = "Conflict", errorBody = errorBody)

            val result = sut.confirmPairing("pairing-beta-alpha", 1, "opt-1")

            result shouldBeEqualTo
                Result.Failure(
                    AppError.DomainError(
                        httpStatus = 409,
                        code = "PAIRING_006",
                        message = "Phương án đã thay đổi, vui lòng tải lại.",
                    ),
                )
        }

    @Test
    fun `when service throws IOException then maps to AppError Network`() =
        runTest {
            coEvery { service.disputePairing("pairing-beta-alpha", any()) } returns
                ApiResult.Exception(IOException("Timeout"))

            val result = sut.disputePairing("pairing-beta-alpha", "Không đúng giờ")

            result shouldBeEqualTo Result.Failure(AppError.Network)
        }
}
