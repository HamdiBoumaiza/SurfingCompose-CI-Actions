package com.hb.surfingcompose.data

sealed class SurfaceResult<out T> {
    data class Success<out T>(val data: T) : SurfaceResult<T>()
    data class Error(val exception: Exception) : SurfaceResult<Nothing>()
    object Loading : SurfaceResult<Nothing>()
}

inline fun <T : Any> SurfaceResult<T>.onSuccess(action: (T) -> Unit): SurfaceResult<T> {
    if (this is SurfaceResult.Success) action(data)
    return this
}

inline fun <T : Any> SurfaceResult<T>.onError(action: (Exception) -> Unit): SurfaceResult<T> {
    if (this is SurfaceResult.Error) action(exception)
    return this
}

inline fun <T : Any> SurfaceResult<T>.onLoading(action: () -> Unit): SurfaceResult<T> {
    if (this is SurfaceResult.Loading) action()
    return this
}