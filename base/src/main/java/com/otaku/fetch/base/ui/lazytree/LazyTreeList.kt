package com.otaku.fetch.base.ui.lazytree


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn
annotation class ExperimentalLazyTreeListApi

@Composable
fun LazyTreeList(
    items: ItemTree,
    modifier: Modifier = Modifier,
    expansionState: ExpansionState = remember { ExpansionState() },
    contentPadding: PaddingValues = PaddingValues(0.dp),
    autoScroll: Boolean = true,
    listState: LazyListState = rememberLazyListState(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    emptyItem: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
    ) {
        val data = items.expand(expansionState)
        if (items.size == 0) {
            item {
                emptyItem()
            }
        } else {
            flattenItems(
                data,
                expansionState,
                onScrollTo = { i ->
                    if (autoScroll) coroutineScope.launch { listState.animateScrollToItem(i) }
                })
        }
    }
}

@ExperimentalLazyTreeListApi
@Composable
fun LazyTreeList(
    modifier: Modifier = Modifier,
    expansionState: ExpansionState = remember { ExpansionState() },
    contentPadding: PaddingValues = PaddingValues(0.dp),
    autoScroll: Boolean = true,
    listState: LazyListState = rememberLazyListState(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: LazyTreeListScope.() -> Unit,
) {
    LazyTreeList(
        items = toItemTree(content) ?: EmptyItemTree,
        modifier = modifier,
        expansionState = expansionState,
        contentPadding = contentPadding,
        autoScroll = autoScroll,
        listState = listState,
        verticalArrangement = verticalArrangement
    ) {
    }
}

@DslMarker
annotation class LazyTreeListMarker

@LazyTreeListMarker
interface LazyTreeListScope {
    fun items(
        count: Int,
        key: (index: Int) -> Any,
        subItems: LazyTreeListScope.(index: Int) -> Unit = {},
        itemContent: @Composable TreeItemScope.(index: Int) -> Unit
    )

    fun item(
        key: Any,
        subItems: LazyTreeListScope.() -> Unit = {},
        itemContent: @Composable TreeItemScope.() -> Unit
    )
}

interface TreeItemScope : LazyItemScope {
    val placement: ItemPlacement
    val depth: Int
    val isExpanded: Boolean
    fun expandItem()
}

internal fun toItemTree(content: (LazyTreeListScope.() -> Unit)): ItemTree? {
    val trees = mutableListOf<ItemTree>()
    content(object : LazyTreeListScope {
        override fun items(
            count: Int,
            key: (index: Int) -> Any,
            subItems: LazyTreeListScope.(index: Int) -> Unit,
            itemContent: @Composable TreeItemScope.(index: Int) -> Unit
        ) {
            trees += object : ItemTree() {
                override val size: Int get() = count

                override fun key(index: Int): Any = key(index)

                @Composable
                override fun LazyItemScope.Item(
                    index: Int,
                    depth: Int,
                    placement: ItemPlacement,
                    expanded: Boolean,
                    onExpand: () -> Unit
                ) {
                    object : TreeItemScope, LazyItemScope by this {
                        override val placement: ItemPlacement get() = placement
                        override val depth: Int get() = depth
                        override val isExpanded: Boolean get() = expanded
                        override fun expandItem() = onExpand()
                    }.run {
                        itemContent(index)
                    }
                }

                override fun expand(index: Int): ItemTree? {
                    val scope: LazyTreeListScope.() -> Unit = { subItems(index) }
                    return toItemTree(scope)
                }
            }
        }

        override fun item(
            key: Any,
            subItems: LazyTreeListScope.() -> Unit,
            itemContent: @Composable TreeItemScope.() -> Unit
        ) {
            items(1, { key }, { subItems() }, { itemContent() })
        }
    })
    return trees.reduceOrNull { acc, items -> acc + items }
}

internal fun concatItems(a: ItemTree, b: ItemTree) = object : ItemTree() {
    override val size: Int get() = a.size + b.size

    override fun key(index: Int): Any {
        return local(index) { items, i -> items.key(i) }
    }

    @Composable
    override fun LazyItemScope.Item(
        index: Int,
        depth: Int,
        placement: ItemPlacement,
        expanded: Boolean,
        onExpand: () -> Unit
    ) {
        local(index) { items, i ->
            with(items) {
                Item(i, depth, placement, expanded, onExpand)
            }
        }
    }

    override fun expand(index: Int): ItemTree? {
        return local(index) { items, i -> items.expand(i) }
    }

    inline fun <R> local(globalIndex: Int, block: (items: ItemTree, localIndex: Int) -> R): R {
        return if (globalIndex < a.size) block(a, globalIndex)
        else block(b, globalIndex - a.size)
    }
}

private operator fun ItemTree.plus(other: ItemTree): ItemTree {
    return when {
        this === EmptyItemTree -> other
        other === EmptyItemTree -> this
        else -> concatItems(this, other)
    }
}