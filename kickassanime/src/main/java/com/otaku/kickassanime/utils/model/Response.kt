package com.otaku.kickassanime.utils.model

sealed class Response<T> {
    var data: T? = null
    var error: Throwable? = null

    constructor(data: T) {
        this.data = data
    }

    constructor(error: Throwable) {
        this.error = error
    }

    class Success<T>(data: T) : Response<T>(data)
    class Error<T>(error: Throwable) : Response<T>(error)
}