package org.voxity.dialer.domain.models

sealed class CallResult {
    object Success : CallResult()
    data class Error(val message: String, val cause: Throwable? = null) : CallResult()
}

sealed class DialerResult<out T> {
    data class Success<T>(val data: T) : DialerResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : DialerResult<Nothing>()
}