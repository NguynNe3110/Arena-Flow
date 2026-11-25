package com.igorwojda.showcase.feature.team.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.team.data.datasource.api.model.InvitationCandidateListDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.InvitationDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.InviteMemberRequestDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.MyInvitationDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.TeamMemberDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

internal interface TeamMemberRetrofitService {
    @GET("api/v1/profile/invitations")
    suspend fun getMyInvitations(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<MyInvitationDto>>

    @POST("api/v1/invitations/{invitationId}/accept")
    suspend fun acceptInvitation(
        @Path("invitationId") invitationId: String,
    ): ApiResult<ApiEnvelope<TeamMemberDto>>

    @POST("api/v1/invitations/{invitationId}/reject")
    suspend fun rejectInvitation(
        @Path("invitationId") invitationId: String,
    ): ApiResult<Unit>

    @GET("api/v1/teams/{teamId}/members")
    suspend fun getTeamMembers(
        @Path("teamId") teamId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<TeamMemberDto>>

    @GET("api/v1/teams/{teamId}/invitation-candidates")
    suspend fun searchInvitationCandidates(
        @Path("teamId") teamId: String,
        @Query("q") query: String,
    ): ApiResult<ApiEnvelope<InvitationCandidateListDto>>

    @POST("api/v1/teams/{teamId}/invitations")
    suspend fun inviteMember(
        @Path("teamId") teamId: String,
        @Body body: InviteMemberRequestDto,
    ): ApiResult<ApiEnvelope<InvitationDto>>

    @POST("api/v1/teams/{teamId}/members/leave")
    suspend fun leaveTeam(
        @Path("teamId") teamId: String,
    ): ApiResult<Unit>
}
