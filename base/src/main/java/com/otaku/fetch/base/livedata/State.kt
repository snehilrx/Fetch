package com.otaku.fetch.base.livedata


sealed class State(val exception: Exception?) {
    class LOADING : State(null)
    class SUCCESS : State(null)
    class FAILED(exception: Exception) : State(exception)
}
