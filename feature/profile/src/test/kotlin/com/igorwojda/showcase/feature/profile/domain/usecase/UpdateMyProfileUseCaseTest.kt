package com.igorwojda.showcase.feature.profile.domain.usecase

import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.profile.domain.model.UpdateProfileParams
import com.igorwojda.showcase.feature.profile.domain.model.UserProfile
import com.igorwojda.showcase.feature.profile.domain.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class UpdateMyProfileUseCaseTest {
    private val profileRepository: ProfileRepository = mockk()
    private val sut = UpdateMyProfileUseCase(profileRepository)

    @Test
    fun `when invoked then returns updated profile from repository`() =
        runTest {
            val params =
                UpdateProfileParams(
                    displayName = "Nguyễn Văn B",
                    preferredPosition = PlayerPosition.MIDFIELDER,
                )
            val expected =
                Result.Success(
                    UserProfile(
                        userId = "user-1",
                        role = AccountRole.USER,
                        providers = emptyList(),
                        phoneNumber = "0987654321",
                        displayName = "Nguyễn Văn B",
                        avatarUrl = null,
                        bio = null,
                        preferredPosition = PlayerPosition.MIDFIELDER,
                        dateOfBirth = null,
                        gender = null,
                        ward = null,
                        createdAt = "2026-11-01T17:00:00",
                        updatedAt = "2026-11-02T17:00:00",
                    ),
                )
            coEvery { profileRepository.updateMyProfile(params) } returns expected

            val actual = sut(params)

            actual shouldBeEqualTo expected
        }
}
