package com.igorwojda.showcase.feature.profile.domain.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.profile.domain.model.CaptainedTeam
import com.igorwojda.showcase.feature.profile.domain.model.Province
import com.igorwojda.showcase.feature.profile.domain.model.UpdateProfileParams
import com.igorwojda.showcase.feature.profile.domain.model.UserProfile
import com.igorwojda.showcase.feature.profile.domain.model.Ward

interface ProfileRepository {
    suspend fun getMyProfile(): Result<UserProfile>

    suspend fun updateMyProfile(params: UpdateProfileParams): Result<UserProfile>

    suspend fun deleteAccount(): Result<Unit>

    suspend fun getProvinces(): Result<List<Province>>

    suspend fun getWardsByProvince(provinceId: String): Result<List<Ward>>

    suspend fun getCaptainedTeams(): Result<List<CaptainedTeam>>
}
