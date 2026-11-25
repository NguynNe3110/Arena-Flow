package com.igorwojda.showcase.feature.notification.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.notification.data.datasource.api.model.NotificationItemDto
import com.igorwojda.showcase.feature.notification.data.datasource.api.model.RegisterFcmTokenRequestDto
import com.igorwojda.showcase.feature.notification.data.datasource.api.model.UnreadCountDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit service for notification endpoints (`NOTIFICATION-001`). */
internal interface NotificationRetrofitService {
    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("unreadOnly") unreadOnly: Boolean? = null,
    ): ApiResult<PaginatedEnvelope<NotificationItemDto>>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): ApiResult<ApiEnvelope<UnreadCountDto>>

    @POST("notifications/{notificationId}/read")
    suspend fun markAsRead(
        @Path("notificationId") notificationId: String,
    ): ApiResult<Unit>

    @POST("notifications/read-all")
    suspend fun markAllAsRead(): ApiResult<Unit>

    @POST("devices/fcm-token")
    suspend fun registerFcmToken(
        @Body request: RegisterFcmTokenRequestDto,
    ): ApiResult<Unit>
}
