package com.otaku.fetch.bindings

import android.content.res.ColorStateList
import androidx.core.view.isEmpty
import androidx.core.view.setPadding
import androidx.databinding.BindingAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.ShapeAppearanceModel
import com.otaku.fetch.base.R
import com.otaku.fetch.base.ui.UiUtils.toPx


object ChipGroupBindings {
    @JvmStatic
    @BindingAdapter("entries")
    fun entries(view: ChipGroup, list: List<String>?) {
        if (list?.isNotEmpty() == true && view.isEmpty()) {
            list.forEach { text ->
                view.addChip(text)
            }
        }
    }

    private fun ChipGroup.addChip(it: String) {
        val chip = Chip(context)
        chip.shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCornerSizes { 15f }.build()
        chip.chipBackgroundColor = ColorStateList.valueOf(-939524096)
        chip.setTextAppearanceResource(R.style.Fetch_ChipText)
        chip.text = it
        chip.setPadding(1.toPx.toInt())
        addView(chip)
    }
}
