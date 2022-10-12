package com.otaku.kickassanime.page.adapters

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.jackandphantom.carouselrecyclerview.CarouselRecyclerview
import com.otaku.kickassanime.R
import com.otaku.kickassanime.utils.Constraints

class CarouselAdapter(private val adapter: Adapter<*>) : Adapter<CarouselAdapter.CarouselRecyclerViewHolder>() {

    private var state: Parcelable? = null

    class CarouselRecyclerViewHolder(
        parent: ViewGroup,
        private val mAdapter: Adapter<*>,
        val recyclerView: CarouselRecyclerview =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.carousel_recycler_view, parent, false) as CarouselRecyclerview
    ) : RecyclerView.ViewHolder(recyclerView) {
        fun bind() {
            recyclerView.apply {
                set3DItem(true)
                setAlpha(true)
                setInfinite(true)
                adapter = mAdapter
                isNestedScrollingEnabled = false
            }
        }
    }

    override fun onViewRecycled(holder: CarouselRecyclerViewHolder) {
        super.onViewRecycled(holder)
        state = holder.recyclerView.layoutManager?.onSaveInstanceState()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselRecyclerViewHolder {
        return  CarouselRecyclerViewHolder(parent, adapter)
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: CarouselRecyclerViewHolder, position: Int) {
        holder.bind()
        if(state != null){
            holder.recyclerView.layoutManager?.onRestoreInstanceState(state)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return Constraints.ITEM_TYPE_CAROUSEL
    }

}
