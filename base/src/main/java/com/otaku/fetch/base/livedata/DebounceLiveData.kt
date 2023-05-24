package com.otaku.fetch.base.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class DebouncedLiveData<T>(
    source: LiveData<T>,
    private val debounceTime: Long,
    context: CoroutineContext = Dispatchers.Main
) : MediatorLiveData<T>(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext = context
        get() = field + job

    init {
        addSource(source) { value ->
            debounceValue(value)
        }
    }

    private fun debounceValue(value: T) {
        // Cancel the previous debounce job if it's still active
        job.cancelChildren()

        launch {
            delay(debounceTime)
            setValue(value)
        }
    }

    override fun onInactive() {
        super.onInactive()
        job.cancel()
    }
}
