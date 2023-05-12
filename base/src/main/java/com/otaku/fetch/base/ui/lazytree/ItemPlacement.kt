package com.otaku.fetch.base.ui.lazytree

@JvmInline
value class ItemPlacement private constructor(private val value: Int) {
    constructor(isFirst: Boolean, isMiddle: Boolean, isLast: Boolean) : this(
        (if (isFirst) 0x1 else 0) or ((if (isMiddle) 0x2 else 0)) or ((if (isLast) 0x4 else 0))
    )

    val isFirst get() = value and 0x1 != 0
    val isMiddle get() = value and 0x2 != 0
    val isLast get() = value and 0x4 != 0
}

fun ItemPlacement(pos: Int, size: Int): ItemPlacement {
    val isFirst = pos == 0
    val isLast = pos == size - 1
    return ItemPlacement(isFirst = isFirst, isMiddle = !isFirst && !isLast, isLast = isLast)
}