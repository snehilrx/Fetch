package com.otaku.kickassanime.page.adapters

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.jackandphantom.carouselrecyclerview.CarouselRecyclerview
import com.otaku.fetch.base.databinding.CarouselItemLayoutBinding
import com.otaku.kickassanime.R
import com.otaku.kickassanime.db.models.AnimeTile
import com.otaku.kickassanime.utils.Constraints

class CarouselAdapter : Adapter<CarouselAdapter.Holder>() {

    private lateinit var holder: Holder
    private var carouselRecyclerview: CarouselRecyclerview? = null
    var state: Parcelable? = null
    private val newAnimeAdapter = AnimeTileAdapterNoPaging<CarouselItemLayoutBinding>(
        com.otaku.fetch.base.R.layout.carousel_item_layout
    ) { binding, item ->
        binding.tileData = item
    }

    class Holder(
        val view: RecyclerView,
    ) : RecyclerView.ViewHolder(view) {
        fun bind(adapter: Adapter<*>) {
            view.adapter = adapter
        }
    }

    override fun getItemId(position: Int): Long {
        return Constraints.ITEM_TYPE_CAROUSEL.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        if (carouselRecyclerview == null) {
            carouselRecyclerview = LayoutInflater.from(parent.context).inflate(
                R.layout.carousel_recycler_view,
                parent,
                false
            ) as CarouselRecyclerview
        }
        holder = Holder(carouselRecyclerview!!)
        holder.setIsRecyclable(false)
        return holder
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(newAnimeAdapter)
        restorePosition()
    }

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        savePosition()
    }

    private fun restorePosition() {
        if (state != null) {
            (holder.view as? RecyclerView)?.layoutManager?.onRestoreInstanceState(state)
        }
    }

    private fun savePosition() {
        if (this::holder.isInitialized)
            state = (holder.view as? RecyclerView)?.layoutManager?.onSaveInstanceState()
    }

    override fun getItemViewType(position: Int): Int {
        return Constraints.ITEM_TYPE_CAROUSEL
    }

    fun submitList(data: List<AnimeTile>?) {
        newAnimeAdapter.submitList(data)
    }

}
