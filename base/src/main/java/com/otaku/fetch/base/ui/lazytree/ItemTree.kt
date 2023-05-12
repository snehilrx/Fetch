package com.otaku.fetch.base.ui.lazytree

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable

abstract class ItemTree {
    abstract val size: Int
    abstract fun key(index: Int): Any

    @Composable
    abstract fun LazyItemScope.Item(
        index: Int,
        depth: Int,
        placement: ItemPlacement,
        expanded: Boolean,
        onExpand: () -> Unit
    )

    abstract fun expand(index: Int): ItemTree?
}

internal object EmptyItemTree : ItemTree() {
    override val size: Int get() = 0

    override fun key(index: Int): Any = error("EmptyItemTree does not contain any elements.")

    @Composable
    override fun LazyItemScope.Item(
        index: Int,
        depth: Int,
        placement: ItemPlacement,
        expanded: Boolean,
        onExpand: () -> Unit
    ) = Unit

    override fun expand(index: Int): ItemTree? = null
}