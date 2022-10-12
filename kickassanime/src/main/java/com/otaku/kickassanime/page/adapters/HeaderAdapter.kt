package com.otaku.kickassanime.page.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.otaku.kickassanime.databinding.HeadingItemBinding
import com.otaku.kickassanime.utils.Constraints

class HeaderAdapter(private val title: CharSequence, private val actionButtonText: CharSequence, private val onClick: () -> Unit) :
    RecyclerView.Adapter<HeaderAdapter.HeadingViewHolder>() {

    class HeadingViewHolder(
        parent: ViewGroup,
        private val headingItemBinding: HeadingItemBinding = HeadingItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ) :
        RecyclerView.ViewHolder(headingItemBinding.root) {

        fun bind(title: CharSequence, actionButtonText: CharSequence, onClick: () -> Unit) {
            headingItemBinding.title = title
            headingItemBinding.actionButtonText = actionButtonText
            headingItemBinding.actionButton.setOnClickListener { onClick() }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeadingViewHolder {
        return HeadingViewHolder(parent)
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: HeadingViewHolder, position: Int) {
        holder.bind(title, actionButtonText, onClick)
    }

    override fun getItemViewType(position: Int): Int {
        return Constraints.ITEM_TYPE_HEADER
    }
}