package com.otaku.fetch.base.ui.lazytree

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable


internal interface LazyItems {
    fun key(index: Int): Any

    @Composable
    fun LazyItemScope.Content(index: Int)
    val size: Int
}

private inline fun lazyItems(
    size: Int,
    depth: Int,
    crossinline key: (index: Int) -> Any,
    crossinline content: @Composable LazyItemScope.(Int) -> Unit,
) = object : LazyItems {
    override fun key(index: Int): Any = listOf(depth, key(index))

    @Composable
    override fun LazyItemScope.Content(index: Int) = content(index)
    override val size: Int get() = size
}

internal fun LazyListScope.flattenItems(
    data: List<TreeExpansion>,
    state: ExpansionState,
    onScrollTo: (index: Int) -> Unit = {}
) {
    val items = buildList {
        flattenItems(data, state, onScrollTo) {
            if (it.size != 0) add(it)
        }
    }
    with(items.flatten()) {
        items(count = size, key = { i -> key(i) }) { i ->
            Content(index = i)
        }
    }
}

private inline fun <R> List<LazyItems>.local(
    global: Int,
    op: (localIndex: Int, items: LazyItems) -> R,
): R {
    var index = global
    for (li in indices) {
        val items = this[li]
        if (index < items.size) return op(index, items) else index -= items.size
    }
    error("$global not found in ${map { it.size }}")
}

private fun List<LazyItems>.flatten(): LazyItems = object : LazyItems {
    override val size: Int = sumOf { it.size }

    override fun key(index: Int): Any {
        return local(index) { i, items -> items.key(i) }
    }

    @Composable
    override fun LazyItemScope.Content(index: Int) {
        local(index) { i, items ->
            items.run { Content(i) }
        }
    }
}

private fun flattenItems(
    data: List<TreeExpansion>, state: ExpansionState,
    onScrollTo: (index: Int) -> Unit,
    depth: Int = 0,
    onItems: (LazyItems) -> Unit,
) {
    val items = data[depth].items
    val expanded = data[depth].expandPos

    with(items) {
        fun expand(pos: Int) {
            state.expand(depth, pos, key(pos))
            if (depth < data.lastIndex) onScrollTo(data[depth].from + pos)
        }

        if (expanded == -1) {
            onItems(lazyItems(size = size, depth = depth, key = { i -> key(i) }) { i ->
                Item(index = i, depth = depth, ItemPlacement(i, size), false) { expand(i) }
            })
        } else {
            onItems(
                lazyItems(
                    size = expanded + 1,
                    depth = depth,
                    key = { i -> key(i) }
                ) { i ->
                    Item(index = i, depth = depth, ItemPlacement(i, size), i == expanded) {
                        if (i != expanded) expand(i) else state.size = depth
                    }
                }
            )
            flattenItems(data, state, onScrollTo, depth + 1, onItems)
            onItems(lazyItems(
                size = size - expanded - 1,
                depth = depth,
                key = { i -> key(i + expanded + 1) }
            ) { i ->
                val gi = i + expanded + 1
                Item(index = gi, depth = depth, ItemPlacement(gi, size), false) { expand(gi) }
            })
        }
    }
}

internal data class TreeExpansion(
    val from: Int,
    val expandedSize: Int,
    val items: ItemTree,
    val expandPos: Int,
)

internal fun ItemTree.expand(state: ExpansionState): List<TreeExpansion> {
    return expand(
        state = state,
        depth = 0,
        result = ArrayList<TreeExpansion>(state.size + 1).apply {
            this += TreeExpansion(
                from = 0, expandedSize = -1,
                items = this@expand, expandPos = -1,
            )
        }
    )
}

internal tailrec fun ItemTree.expand(
    state: ExpansionState, depth: Int,
    result: MutableList<TreeExpansion>,
): List<TreeExpansion> {
    val expandPos = state.getOrNull(depth)
    val subTree = expandPos?.let { (index, key) ->
        if (key(index) != key) {
            state.size = depth
            null
        } else expand(index)
    }
    if (subTree == null) {
        result[result.lastIndex] = result.last().let { it.copy(expandedSize = it.items.size) }
        for (i in result.lastIndex - 1 downTo 0) result[i] = result[i].let {
            it.copy(expandedSize = it.items.size + result[i + 1].expandedSize)
        }
        return result
    }
    result[result.lastIndex] = result.last().copy(expandPos = expandPos.index)
    result += TreeExpansion(
        from = result.last().let { it.from + it.expandPos + 1 }, expandedSize = -1,
        items = subTree, expandPos = -1
    )
    return subTree.expand(state, depth + 1, result)
}
