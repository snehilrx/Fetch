package com.otaku.kickassanime.page.adapters

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.otaku.fetch.base.utils.UiUtils.toPxInt

class GridSpacingItemDecoration(
    private val itemWidth: Int,
    private val spanCount: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // item position
        if (position >= 0 && parent.getChildViewHolder(view) is AnimeTileAdapter.AnimeTileViewHolder<*>) {
            val betweenSpacing = 20.toPxInt
            val spacing = parent.width - itemWidth * spanCount - betweenSpacing
            val column = position % spanCount // item column
            outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
            outRect.right =
                spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            if (position >= spanCount) {
                outRect.top = betweenSpacing // item top
            }
        } else {
            outRect.left = 0
            outRect.right = 0
            outRect.top = 0
            outRect.bottom = 0
        }
    }
}