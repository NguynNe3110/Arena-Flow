package com.igorwojda.showcase.feature.profile.data.datasource.api.model

import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.profile.domain.model.AuthProvider
import com.igorwojda.showcase.feature.profile.domain.model.CaptainedTeam
import com.igorwojda.showcase.feature.profile.domain.model.ProfileGender
import com.igorwojda.showcase.feature.profile.domain.model.ProfileWard
import com.igorwojda.showcase.feature.profile.domain.model.Province
import com.igorwojda.showcase.feature.profile.domain.model.UserProfile
import com.igorwojda.showcase.feature.profile.domain.model.Ward
import kotlinx.serialization.Serializable

@Serializable
data class AuthProviderDto(
    val provider: String,
    val providerUserId: String,
)

@Serializable
data class ProfileWardDto(
    val wardId: String,
    val wardName: String,
    val provinceId: String,
    val provinceName: String,
)

@Serializable
data class ProfileResponseDto(
    val userId: String,
    val role: String,
    val providers: List<AuthProviderDto> = emptyList(),
    val phoneNumber: String? = null,
    val displayName: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val preferredPosition: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val ward: ProfileWardDto? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class UpdateProfileRequestDto(
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val preferredPosition: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val wardId: String? = null,
)

@Serializable
data class ProvinceItemDto(
    val provinceId: String,
    val provinceName: String,
)

@Serializable
data class WardItemDto(
    val wardId: String,
    val wardName: String,
)

@Serializable
data class MyTeamItemDto(
    val teamId: String,
    val teamName: String,
    val avatarUrl: String? = null,
    val skillLevels: List<String> = emptyList(),
    val status: String,
    val captainName: String? = null,
    val memberCount: Int,
    val role: String,
    val joinedAt: String,
)

internal fun ProfileResponseDto.toDomain(): UserProfile =
    UserProfile(
        userId = userId,
        role = if (role.equals("ADMIN", ignoreCase = true)) AccountRole.ADMIN else AccountRole.USER,
        providers = providers.map { AuthProvider(provider = it.provider, providerUserId = it.providerUserId) },
        phoneNumber = phoneNumber,
        displayName = displayName,
        avatarUrl = avatarUrl,
        bio = bio,
        preferredPosition = PlayerPosition.fromApiLabel(preferredPosition),
        dateOfBirth = dateOfBirth,
        gender = ProfileGender.fromApiValue(gender),
        ward =
            ward?.let {
                ProfileWard(
                    wardId = it.wardId,
                    wardName = it.wardName,
                    provinceId = it.provinceId,
                    provinceName = it.provinceName,
                )
            },
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

internal fun ProvinceItemDto.toDomain(): Province =
    Province(
        provinceId = provinceId,
        provinceName = provinceName,
    )

internal fun WardItemDto.toDomain(): Ward =
    Ward(
        wardId = wardId,
        wardName = wardName,
    )

internal fun MyTeamItemDto.toDomain(): CaptainedTeam =
    CaptainedTeam(
        teamId = teamId,
        teamName = teamName,
        avatarUrl = avatarUrl,
        status = status,
        memberCount = memberCount,
        joinedAt = joinedAt,
    )
