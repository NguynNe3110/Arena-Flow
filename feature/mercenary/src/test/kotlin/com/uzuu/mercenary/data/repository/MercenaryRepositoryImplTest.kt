package com.uzuu.mercenary.data.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.mercenary.data.datasource.api.MercenaryRemoteDataSource
import com.uzuu.mercenary.domain.model.MercenaryPost
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MercenaryRepositoryImplTest {
    private val mercenaryRemoteDataSource: MercenaryRemoteDataSource = mockk()

    private val sut = MercenaryRepositoryImpl(mercenaryRemoteDataSource)

    @Test
    fun `getMercenaryPosts returns success when datasource succeeds`() =
        runTest {
            val posts =
                listOf(
                    MercenaryPost(
                        id = "rec-1",
                        teamName = "FC",
                        captainName = "Cap",
                        position = "GK",
                        positionLabel = "Thủ môn",
                        matchDate = "T7",
                        timeSlot = "19:00",
                        estimatedArea = "Area",
                        distanceKm = 1.0,
                        costShare = "Free",
                        requirements = "Req",
                    ),
                )
            coEvery { mercenaryRemoteDataSource.getMercenaryPosts() } returns posts

            val result = sut.getMercenaryPosts()

            assertEquals(Result.Success(posts), result)
        }

    @Test
    fun `applyMercenary returns success when datasource succeeds`() =
        runTest {
            val post =
                MercenaryPost(
                    id = "rec-1",
                    teamName = "FC",
                    captainName = "Cap",
                    position = "GK",
                    positionLabel = "Thủ môn",
                    matchDate = "T7",
                    timeSlot = "19:00",
                    estimatedArea = "Area",
                    distanceKm = 1.0,
                    costShare = "Free",
                    requirements = "Req",
                    isApplied = true,
                )
            coEvery { mercenaryRemoteDataSource.applyMercenary("rec-1", "msg") } returns post

            val result = sut.applyMercenary("rec-1", "msg")

            assertEquals(Result.Success(post), result)
        }

    @Test
    fun `getMercenaryPosts maps exception to failure`() =
        runTest {
            val exception = RuntimeException("Network error")
            coEvery { mercenaryRemoteDataSource.getMercenaryPosts() } throws exception

            val result = sut.getMercenaryPosts()

            assertTrue(result is Result.Failure)
        }
}
