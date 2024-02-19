package com.example.videosdk.network

import retrofit2.Response

suspend fun <T> getResult(call: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error("Invalid response from the server", ErrorType.InvalidData)
            }

        } else {
            val errorMessage = try {
                response.errorBody()?.string() ?: "Something went wrong"
            } catch (e: Exception) {
                "Something went wrong"
            }

            when (response.code()) {
                400 -> ApiResult.Error(
                    errorMessage,
                    ErrorType.HttpError.BadRequest(response.code())
                )
                401 -> ApiResult.Error(
                    errorMessage,
                    ErrorType.HttpError.Unauthorized(response.code())
                )
                403 -> ApiResult.Error(
                    errorMessage,
                    ErrorType.HttpError.ResourceForbidden(response.code())
                )
                404 -> ApiResult.Error(
                    errorMessage,
                    ErrorType.HttpError.ResourceNotFound(response.code())
                )
                500 -> ApiResult.Error(
                    errorMessage,
                    ErrorType.HttpError.InternalServerError(response.code())
                )
                502 -> ApiResult.Error(
                    errorMessage,
                    ErrorType.HttpError.BadGateWay(response.code())
                )
                301 -> ApiResult.Error(
                    errorMessage,
                    ErrorType.HttpError.ResourceRemoved(response.code())
                )
                302 -> ApiResult.Error(
                    errorMessage,
                    ErrorType.HttpError.RemovedResourceFound(response.code())
                )
                else -> ApiResult.Error(
                    errorMessage,
                    ErrorType.HttpError.OtherError(response.code())
                )
            }
        }
    } catch (throwable: Throwable) {
        ApiResult.Error(throwable.localizedMessage!!, ErrorType.NetworkException)
    }
}

sealed class ApiResult<out T> {

    data class Success<out T>(val data: T) : ApiResult<T>()

    data class Error(val message: String, val type: ErrorType) : ApiResult<Nothing>()
}

sealed class ErrorType {
    object InvalidData : ErrorType()
    object NetworkException : ErrorType()

    sealed class HttpError(open val statusCode: Int) : ErrorType() {

        data class BadRequest(override val statusCode: Int) : HttpError(statusCode)
        data class Unauthorized(override val statusCode: Int) : HttpError(statusCode)
        data class ResourceForbidden(override val statusCode: Int) : HttpError(statusCode)
        data class ResourceNotFound(override val statusCode: Int) : HttpError(statusCode)
        data class InternalServerError(override val statusCode: Int) : HttpError(statusCode)
        data class BadGateWay(override val statusCode: Int) : HttpError(statusCode)
        data class ResourceRemoved(override val statusCode: Int) : HttpError(statusCode)
        data class RemovedResourceFound(override val statusCode: Int) : HttpError(statusCode)
        data class OtherError(override val statusCode: Int) : HttpError(statusCode)
    }
}


sealed class ViewState<out T> {
    object Loading : ViewState<Nothing>()
    data class Error(val errorMessage: String) : ViewState<Nothing>()
    data class Success<T>(val data: T) : ViewState<T>()
}