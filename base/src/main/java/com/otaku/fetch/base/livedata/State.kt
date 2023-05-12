package com.otaku.fetch.base.livedata


sealed class GenericState<T>(open val exception: Exception?, obj: T?) {
    @Suppress("unused")
    class LOADING<T> : GenericState<T>(null, null)

    @Suppress("unused")
    class SUCCESS<T>(data: T? = null) : GenericState<T>(null, data)

    @Suppress("unused")
    class FAILED<T>(exception: Exception) : GenericState<T>(exception, null)
}

sealed class State(override val exception: Exception?) : GenericState<Any>(exception, null) {
    class LOADING : State(null)
    class SUCCESS : State(null)
    class FAILED(exception: Exception, val shouldTerminateActivity: Boolean = true) :
        State(exception)
}