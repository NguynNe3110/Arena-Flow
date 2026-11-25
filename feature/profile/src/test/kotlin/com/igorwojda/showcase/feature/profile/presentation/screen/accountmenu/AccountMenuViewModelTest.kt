package com.igorwojda.showcase.feature.profile.presentation.screen.accountmenu

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.model.SessionUserSummary
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.profile.domain.model.CaptainedTeam
import com.igorwojda.showcase.feature.profile.domain.usecase.DeleteAccountUseCase
import com.igorwojda.showcase.feature.profile.domain.usecase.GetCaptainedTeamsForDeleteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
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
class AccountMenuViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val deleteAccountUseCase: DeleteAccountUseCase = mockk()
    private val getCaptainedTeamsForDeleteUseCase: GetCaptainedTeamsForDeleteUseCase = mockk()
    private val sessionRepository: SessionRepository = mockk(relaxed = true)
    private lateinit var sut: AccountMenuViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { sessionRepository.observeSession() } returns
            flowOf(
                ArenaSessionState(
                    currentUser =
                        SessionUserSummary(
                            id = "user-cap-alpha",
                            phone = "0987654321",
                            displayName = "Nguyễn Văn A",
                            preferredPosition = null,
                            wardId = null,
                            role = AccountRole.USER,
                            isNewUser = false,
                            reputationScore = 92,
                            matchCount = 25,
                        ),
                ),
            )
        sut =
            AccountMenuViewModel(
                deleteAccountUseCase = deleteAccountUseCase,
                getCaptainedTeamsForDeleteUseCase = getCaptainedTeamsForDeleteUseCase,
                sessionRepository = sessionRepository,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when confirmLogout invoked then clears session and marks logged out`() =
        runTest {
            sut.confirmLogout()

            coVerify(exactly = 1) { sessionRepository.logout() }
            sut.uiStateFlow.value.isLoggedOut shouldBeEqualTo true
        }

    @Test
    fun `when deleteAccount succeeds then logs out and marks account deleted`() =
        runTest {
            coEvery { deleteAccountUseCase() } returns Result.Success(Unit)

            sut.confirmDeleteAccount()

            coVerify(exactly = 1) { sessionRepository.logout() }
            sut.uiStateFlow.value.isAccountDeleted shouldBeEqualTo true
        }

    @Test
    fun `when deleteAccount blocked with 422 USER_003 then loads captained teams and shows sheet`() =
        runTest {
            val blockingTeams =
                listOf(
                    CaptainedTeam(
                        teamId = "team-alpha",
                        teamName = "FC Sao Mai",
                        avatarUrl = null,
                        status = "ACTIVE",
                        memberCount = 5,
                        joinedAt = "2026-10-01T10:00:00",
                    ),
                )
            coEvery { deleteAccountUseCase() } returns
                Result.Failure(
                    AppError.DomainError(
                        httpStatus = 422,
                        code = "USER_003",
                        message = "Còn là đội trưởng",
                    ),
                )
            coEvery { getCaptainedTeamsForDeleteUseCase() } returns Result.Success(blockingTeams)

            sut.confirmDeleteAccount()

            sut.uiStateFlow.value.showBlockedCaptainSheet shouldBeEqualTo true
            sut.uiStateFlow.value.blockingTeams shouldBeEqualTo blockingTeams
            coVerify(exactly = 0) { sessionRepository.logout() }
        }
}
