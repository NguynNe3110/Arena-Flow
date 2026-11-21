package com.uzuu.pairing.data.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.pairing.data.datasource.api.PairingRemoteDataSource
import com.uzuu.pairing.domain.model.PairingRoom
import com.uzuu.pairing.domain.model.PairingTeam
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PairingRepositoryImplTest {
    private val pairingRemoteDataSource: PairingRemoteDataSource = mockk()

    private val sut = PairingRepositoryImpl(pairingRemoteDataSource)

    private val room =
        PairingRoom(
            pairingId = "pair-1",
            matchId = "match-1",
            hostTeam = PairingTeam("h", "Host", "H", 90),
            guestTeam = PairingTeam("g", "Guest", "G", 90),
            venueOptions = emptyList(),
            selectedVenueId = "v1",
            timeOptions = listOf("19:00"),
            selectedTimeSlot = "19:00",
        )

    @Test
    fun `getPairingRoom returns success when datasource succeeds`() =
        runTest {
            coEvery { pairingRemoteDataSource.getPairingRoom("pair-1") } returns room

            val result = sut.getPairingRoom("pair-1")

            assertEquals(Result.Success(room), result)
        }

    @Test
    fun `updatePackageOption returns success when datasource succeeds`() =
        runTest {
            coEvery { pairingRemoteDataSource.updatePackageOption("pair-1", "v2", "20:00") } returns room

            val result = sut.updatePackageOption("pair-1", "v2", "20:00")

            assertEquals(Result.Success(room), result)
        }

    @Test
    fun `confirmPackage returns success when datasource succeeds`() =
        runTest {
            coEvery { pairingRemoteDataSource.confirmPackage("pair-1") } returns room

            val result = sut.confirmPackage("pair-1")

            assertEquals(Result.Success(room), result)
        }

    @Test
    fun `getPairingRoom maps exception to failure`() =
        runTest {
            val exception = RuntimeException("Network error")
            coEvery { pairingRemoteDataSource.getPairingRoom("pair-1") } throws exception

            val result = sut.getPairingRoom("pair-1")

            assertTrue(result is Result.Failure)
        }
}
