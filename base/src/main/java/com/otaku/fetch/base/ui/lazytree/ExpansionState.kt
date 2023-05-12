package com.otaku.fetch.base.ui.lazytree

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf

@Stable
interface ExpansionState {
    interface Entry {
        val index: Int
        val key: Any
    }

    var size: Int
    operator fun get(depth: Int): Entry
    fun expand(depth: Int, index: Int, key: Any)
}

internal operator fun ExpansionState.Entry.component1() = index
internal operator fun ExpansionState.Entry.component2() = key

fun ExpansionState(initial: List<ExpansionState.Entry> = emptyList()): ExpansionState {
    val state = mutableStateOf(initial)
    return ExpansionState(getState = { state.value }, setState = { state.value = it })
}

private data class ExpansionEntry(
    override val index: Int,
    override val key: Any,
) : ExpansionState.Entry

internal fun ExpansionState(
    getState: () -> List<ExpansionState.Entry>,
    setState: (List<ExpansionState.Entry>) -> Unit,
): ExpansionState = object : ExpansionState {
    var data
        get() = getState()
        set(value) {
            setState(value)
        }

    override var size: Int
        get() = data.size
        set(value) {
            if (value >= size) return
            data = data.take(value)
        }

    override fun get(depth: Int): ExpansionState.Entry {
        return data[depth]
    }

    override fun expand(depth: Int, index: Int, key: Any) {
        if (depth > data.size) return
        data = data.take(depth) + ExpansionEntry(index, key)
    }
}

internal fun ExpansionState.getOrNull(depth: Int) = if (depth in 0 until size) get(depth) else null
