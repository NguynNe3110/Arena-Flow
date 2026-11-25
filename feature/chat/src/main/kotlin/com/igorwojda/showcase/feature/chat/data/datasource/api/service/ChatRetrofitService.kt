package com.igorwojda.showcase.feature.chat.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.CursorListDto
import com.igorwojda.showcase.feature.chat.data.datasource.api.model.ChatMessageDto
import com.igorwojda.showcase.feature.chat.data.datasource.api.model.ChatRoomDetailDto
import com.igorwojda.showcase.feature.chat.data.datasource.api.model.ChatRoomSummaryDto
import com.igorwojda.showcase.feature.chat.data.datasource.api.model.ReportChatRequestDto
import com.igorwojda.showcase.feature.chat.data.datasource.api.model.SendMessageRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit service for chat rooms, messages, read receipts, and chat reports. */
interface ChatRetrofitService {
    @GET("chats")
    suspend fun getChatRooms(
        @Query("contextType") contextType: String? = null,
    ): ApiResult<ApiEnvelope<List<ChatRoomSummaryDto>>>

    @GET("chats/{roomId}")
    suspend fun getChatRoomDetail(
        @Path("roomId") roomId: String,
    ): ApiResult<ApiEnvelope<ChatRoomDetailDto>>

    @GET("chats/{roomId}/messages")
    suspend fun getMessages(
        @Path("roomId") roomId: String,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 50,
    ): ApiResult<ApiEnvelope<CursorListDto<ChatMessageDto>>>

    @POST("chats/{roomId}/messages")
    suspend fun sendMessage(
        @Path("roomId") roomId: String,
        @Body request: SendMessageRequestDto,
    ): ApiResult<ApiEnvelope<ChatMessageDto>>

    @POST("chats/{roomId}/read")
    suspend fun markAsRead(
        @Path("roomId") roomId: String,
    ): ApiResult<Unit>

    @POST("chats/{roomId}/reports")
    suspend fun reportChat(
        @Path("roomId") roomId: String,
        @Body request: ReportChatRequestDto,
    ): ApiResult<Unit>
}
