package com.otaku.fetch.base.livedata


sealed class GenericState<T>(open val exception: Exception?, val obj: T?) {

    class LOADING<T> : GenericState<T>(null, null)

    class SUCCESS<T>(data: T? = null) : GenericState<T>(null, data)

    class FAILED<T>(exception: Exception) : GenericState<T>(exception, null)

    override fun equals(other: Any?): Boolean {
        return if (other is GenericState<*>) {
            other is LOADING<*> && this is LOADING || other is SUCCESS<*> && this is SUCCESS ||
                    other is FAILED<*> && this is FAILED
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = exception?.hashCode() ?: 0
        result = 31 * result + (obj?.hashCode() ?: 0)
        return result
    }
}

sealed class State(override val exception: Exception?) : GenericState<Any>(exception, null) {
    class LOADING : State(null)
    class SUCCESS : State(null)
    class FAILED(exception: Exception) :
        State(exception)
}