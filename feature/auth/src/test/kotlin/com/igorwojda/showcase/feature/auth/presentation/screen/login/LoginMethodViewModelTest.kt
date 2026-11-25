package com.igorwojda.showcase.feature.auth.presentation.screen.login

import com.igorwojda.showcase.feature.auth.domain.model.AuthSession
import com.igorwojda.showcase.feature.auth.domain.model.OAuthProvider
import com.igorwojda.showcase.feature.auth.domain.usecase.SignInWithOAuthUseCase
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class LoginMethodViewModelTest {
    private val signInWithOAuthUseCase: SignInWithOAuthUseCase = mockk()
    private val sut = LoginMethodViewModel(signInWithOAuthUseCase)

    @Test
    fun `signInWithOAuth success invokes onAuthSuccess with isNewUser`() =
        runTest {
            val session =
                AuthSession(
                    userId = "u1",
                    role = AccountRole.USER,
                    isNewUser = true,
                    accessToken = "a",
                    refreshToken = "r",
                    expiresInSeconds = 3600,
                )
            coEvery { signInWithOAuthUseCase(OAuthProvider.GOOGLE, "tok", "a@b.com") } returns Result.Success(session)
            var capturedIsNew: Boolean? = null

            sut.signInWithOAuth(OAuthProvider.GOOGLE, "tok", "a@b.com") { capturedIsNew = it }
            advanceUntilIdle()

            assertEquals(true, capturedIsNew)
            assertNull(sut.uiStateFlow.value.loadingProvider)
        }

    @Test
    fun `signInWithOAuth AUTH_003 failure sets deleted account state`() =
        runTest {
            coEvery {
                signInWithOAuthUseCase(OAuthProvider.FACEBOOK, "tok", "del@b.com")
            } returns Result.Failure(AppError.DomainError(403, "AUTH_003", "Tài khoản đã bị xóa"))

            sut.signInWithOAuth(OAuthProvider.FACEBOOK, "tok", "del@b.com") {}
            advanceUntilIdle()

            assertTrue(sut.uiStateFlow.value.isDeletedAccountError)
            assertEquals("Tài khoản đã bị xóa", sut.uiStateFlow.value.errorMessage)
        }
}
