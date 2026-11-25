package com.igorwojda.showcase.feature.profile.domain.model

import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition

enum class ProfileGender(
    val apiValue: String,
    val label: String,
) {
    MALE("MALE", "Nam"),
    FEMALE("FEMALE", "Nữ"),
    OTHER("OTHER", "Khác"),
    ;

    companion object {
        fun fromApiValue(value: String?): ProfileGender? = entries.firstOrNull { it.apiValue.equals(value?.trim(), ignoreCase = true) }
    }
}

data class AuthProvider(
    val provider: String,
    val providerUserId: String,
)

data class ProfileWard(
    val wardId: String,
    val wardName: String,
    val provinceId: String,
    val provinceName: String,
) {
    val fullLabel: String
        get() = "$wardName, $provinceName"
}

data class UserProfile(
    val userId: String,
    val role: AccountRole,
    val providers: List<AuthProvider>,
    val phoneNumber: String?,
    val displayName: String,
    val avatarUrl: String?,
    val bio: String?,
    val preferredPosition: PlayerPosition?,
    val dateOfBirth: String?,
    val gender: ProfileGender?,
    val ward: ProfileWard?,
    val createdAt: String,
    val updatedAt: String,
)

data class UpdateProfileParams(
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val preferredPosition: PlayerPosition? = null,
    val dateOfBirth: String? = null,
    val gender: ProfileGender? = null,
    val wardId: String? = null,
)

data class Province(
    val provinceId: String,
    val provinceName: String,
)

data class Ward(
    val wardId: String,
    val wardName: String,
)

data class CaptainedTeam(
    val teamId: String,
    val teamName: String,
    val avatarUrl: String?,
    val status: String,
    val memberCount: Int,
    val joinedAt: String,
)
