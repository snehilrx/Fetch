package com.otaku.kickassanime.page.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IntRange
import androidx.core.view.setPadding
import androidx.recyclerview.widget.*
import com.google.android.material.textview.MaterialTextView
import com.jackandphantom.carouselrecyclerview.CarouselRecyclerview
import com.otaku.fetch.base.ui.UiUtils.toPx
import com.otaku.kickassanime.R
import com.otaku.kickassanime.page.adapters.ItemListAdapter.Companion.VIEW_TYPE_CAROUSEL
import com.otaku.kickassanime.page.adapters.ItemListAdapter.Companion.VIEW_TYPE_HEADER
import com.otaku.kickassanime.page.adapters.ItemListAdapter.Companion.VIEW_TYPE_LIST

class ItemListAdapter :
    ListAdapter<ItemListAdapter.Item, ItemListAdapter.ItemViewHolder>(diffCallback) {

    private val viewPool = RecyclerView.RecycledViewPool()

    interface Item {
        fun getItemViewType(): Int
        fun areItemsTheSame(newItem: Item): Boolean
        fun areContentsTheSame(newItem: Item): Boolean
    }

    abstract class ItemViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {
        abstract fun bind(item: Item?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeadingViewHolder(parent)
            VIEW_TYPE_LIST -> RecyclerViewHolder(
                parent,
                LinearLayoutManager(parent.context).apply {
                    recycleChildrenOnDetach = true
                },
                viewPool
            )

            VIEW_TYPE_GRID -> {
                val spanCount =
                    parent.resources.getInteger(com.otaku.fetch.base.R.integer.span_count)
                RecyclerViewHolder(
                    parent,
                    GridLayoutManager(parent.context, spanCount).apply {
                        recycleChildrenOnDetach = true
                    },
                    viewPool
                )
            }

            VIEW_TYPE_CAROUSEL -> CarouselRecyclerViewHolder(parent)
            else -> throw IllegalStateException("unknown view type")
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).getItemViewType()
    }

    ///////////////////////////////////////////////////////////////////////////
    // View type constraints
    ///////////////////////////////////////////////////////////////////////////
    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_LIST = 1
        const val VIEW_TYPE_GRID = 2
        const val VIEW_TYPE_CAROUSEL = 3

        @JvmStatic
        private val diffCallback = object : DiffUtil.ItemCallback<Item>() {

            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return if (oldItem.getItemViewType() == newItem.getItemViewType()) true
                else newItem.areItemsTheSame(newItem)
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return if (oldItem.getItemViewType() == newItem.getItemViewType()) true
                else newItem.areContentsTheSame(newItem)
            }
        }
    }


}

class HeadingViewHolder(
    parent: ViewGroup,
    private val textView: TextView = MaterialTextView(parent.context).apply {
        setPadding(24.toPx.toInt())
        setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_ActionBar_Title)
    }
) :
    ItemListAdapter.ItemViewHolder(textView as View) {

    override fun bind(item: ItemListAdapter.Item?) {
        if (item is Heading) {
            textView.text = item.title
        }
    }

    data class Heading(val title: CharSequence) : ItemListAdapter.Item {
        override fun getItemViewType(): Int = VIEW_TYPE_HEADER
        override fun areItemsTheSame(newItem: ItemListAdapter.Item): Boolean {
            return true
        }

        override fun areContentsTheSame(newItem: ItemListAdapter.Item): Boolean {
            return this == newItem
        }
    }
}

class RecyclerViewHolder(
    parent: ViewGroup,
    private val layoutManagerNew: RecyclerView.LayoutManager,
    private val viewPool: RecyclerView.RecycledViewPool,
    private val recyclerView: RecyclerView = RecyclerView(parent.context)
) : ItemListAdapter.ItemViewHolder(recyclerView) {

    override fun bind(item: ItemListAdapter.Item?) {
        if (item is SimpleItem) {
            recyclerView.apply {
                setRecycledViewPool(viewPool)
                adapter = item.adapter
                itemAnimator = item.animator
                layoutManager = layoutManagerNew
                addItemDecoration(
                    GridSpacingItemDecoration(
                        resources.getInteger(com.otaku.fetch.base.R.integer.span_count),
                        resources.getDimension(com.otaku.fetch.base.R.dimen.item_spacing).toInt(),
                        true,
                        0
                    )
                )
                item.decorator?.let { addItemDecoration(it) }
                isNestedScrollingEnabled = false
            }
        }
    }
}

class CarouselRecyclerViewHolder(
    parent: ViewGroup,
    private val recyclerView: CarouselRecyclerview =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.carousel_recycler_view, parent, false) as CarouselRecyclerview
) : ItemListAdapter.ItemViewHolder(recyclerView) {

    override fun bind(item: ItemListAdapter.Item?) {
        if (item is SimpleItem) {
            recyclerView.apply {
                set3DItem(true)
                setAlpha(true)
                setInfinite(true)
                item.decorator?.let { addItemDecoration(it) }
                adapter = item.adapter
                itemAnimator = item.animator
                isNestedScrollingEnabled = false
            }
        }
    }
}

class SimpleItem(
    @IntRange(
        from = VIEW_TYPE_LIST.toLong(),
        to = VIEW_TYPE_CAROUSEL.toLong()
    ) val itemType: Int,
    val adapter: RecyclerView.Adapter<*>,
    val decorator: RecyclerView.ItemDecoration? = null,
    val animator: RecyclerView.ItemAnimator? = null
) : ItemListAdapter.Item {
    override fun getItemViewType(): Int = itemType

    override fun areItemsTheSame(newItem: ItemListAdapter.Item): Boolean {
        if (newItem !is SimpleItem) return false
        return newItem.getItemViewType() == getItemViewType()
    }

    override fun areContentsTheSame(newItem: ItemListAdapter.Item): Boolean {
        return this == newItem
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleItem) return false

        if (itemType != other.itemType) return false
        if (adapter != other.adapter) return false
        if (decorator != other.decorator) return false
        if (animator != other.animator) return false

        return true
    }

    override fun hashCode(): Int {
        var result = itemType
        result = 31 * result + adapter.hashCode()
        result = 31 * result + decorator.hashCode()
        result = 31 * result + animator.hashCode()
        return result
    }
}