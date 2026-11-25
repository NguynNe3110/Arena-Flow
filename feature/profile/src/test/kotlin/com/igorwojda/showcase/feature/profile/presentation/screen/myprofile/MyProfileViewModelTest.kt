package com.igorwojda.showcase.feature.profile.presentation.screen.myprofile

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.ActingTeamOption
import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.model.SessionUserSummary
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.profile.domain.model.UserProfile
import com.igorwojda.showcase.feature.profile.domain.usecase.GetMyProfileUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyProfileViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val getMyProfileUseCase: GetMyProfileUseCase = mockk()
    private val getSessionStateUseCase: GetSessionStateUseCase = mockk()
    private lateinit var sut: MyProfileViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when initialized with authenticated user then loads profile and session summary`() =
        runTest {
            val profile =
                UserProfile(
                    userId = "user-cap-alpha",
                    role = AccountRole.ADMIN,
                    providers = emptyList(),
                    phoneNumber = "0987654321",
                    displayName = "Nguyễn Văn A",
                    avatarUrl = null,
                    bio = "Fair play",
                    preferredPosition = PlayerPosition.FORWARD,
                    dateOfBirth = "1995-10-20",
                    gender = null,
                    ward = null,
                    createdAt = "2026-11-01T17:00:00",
                    updatedAt = "2026-11-02T17:00:00",
                )
            val teams = listOf(ActingTeamOption("team-alpha", "FC Sao Mai", "An Khánh", 92, "ACTIVE"))
            every { getSessionStateUseCase() } returns
                flowOf(
                    ArenaSessionState(
                        currentUser =
                            SessionUserSummary(
                                id = "user-cap-alpha",
                                phone = "0987654321",
                                displayName = "Nguyễn Văn A",
                                preferredPosition = PlayerPosition.FORWARD,
                                wardId = "ward-ak",
                                role = AccountRole.ADMIN,
                                isNewUser = false,
                                reputationScore = 92,
                                matchCount = 25,
                            ),
                        captainedTeams = teams,
                    ),
                )
            coEvery { getMyProfileUseCase() } returns Result.Success(profile)

            sut = MyProfileViewModel(getMyProfileUseCase, getSessionStateUseCase)

            sut.uiStateFlow.value.isLoading shouldBeEqualTo false
            sut.uiStateFlow.value.isGuest shouldBeEqualTo false
            sut.uiStateFlow.value.profile shouldBeEqualTo profile
            sut.uiStateFlow.value.reputationScore shouldBeEqualTo 92
            sut.uiStateFlow.value.matchCount shouldBeEqualTo 25
            sut.uiStateFlow.value.myTeams shouldBeEqualTo teams
            sut.isAdmin() shouldBeEqualTo true
        }

    @Test
    fun `when profile returns unauthorized then switches to guest state`() =
        runTest {
            every { getSessionStateUseCase() } returns flowOf(ArenaSessionState(currentUser = null))
            coEvery { getMyProfileUseCase() } returns Result.Failure(AppError.Unauthorized)

            sut = MyProfileViewModel(getMyProfileUseCase, getSessionStateUseCase)

            sut.uiStateFlow.value.isLoading shouldBeEqualTo false
            sut.uiStateFlow.value.isGuest shouldBeEqualTo true
            sut.uiStateFlow.value.profile shouldBeEqualTo null
        }
}
