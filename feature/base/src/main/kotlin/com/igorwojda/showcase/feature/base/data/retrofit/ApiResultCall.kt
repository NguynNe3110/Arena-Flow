package com.igorwojda.showcase.feature.base.data.retrofit

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class ApiResultCall<T> constructor(
    private val callDelegate: Call<T>,
) : Call<ApiResult<T>> {
    @Suppress("detekt.MagicNumber")
    override fun enqueue(callback: Callback<ApiResult<T>>) =
        callDelegate.enqueue(
            object : Callback<T> {
                override fun onResponse(
                    call: Call<T>,
                    response: Response<T>,
                ) {
                    val body = response.body()
                    val result: ApiResult<T> =
                        when {
                            response.isSuccessful && body != null -> {
                                ApiResult.Success(body)
                            }

                            response.isSuccessful && (response.code() == 204 || response.code() == 205) -> {
                                @Suppress("UNCHECKED_CAST")
                                ApiResult.Success(Unit as T)
                            }

                            else -> {
                                val rawError = runCatching { response.errorBody()?.string() }.getOrNull()
                                ApiResult.Error(response.code(), response.message(), rawError)
                            }
                        }
                    callback.onResponse(this@ApiResultCall, Response.success(result))
                }

                override fun onFailure(
                    call: Call<T>,
                    throwable: Throwable,
                ) {
                    if (call.isCanceled || throwable is kotlinx.coroutines.CancellationException) {
                        callback.onFailure(this@ApiResultCall, throwable)
                    } else {
                        callback.onResponse(this@ApiResultCall, Response.success(ApiResult.Exception(throwable)))
                    }
                }
            },
        )

    override fun clone(): Call<ApiResult<T>> = ApiResultCall(callDelegate.clone())

    override fun execute(): Response<ApiResult<T>> = throw UnsupportedOperationException("ResponseCall does not support execute.")

    override fun isExecuted(): Boolean = callDelegate.isExecuted

    override fun cancel() = callDelegate.cancel()

    override fun isCanceled(): Boolean = callDelegate.isCanceled

    override fun request(): Request = callDelegate.request()

    override fun timeout(): Timeout = callDelegate.timeout()
}
