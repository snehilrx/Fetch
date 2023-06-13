package com.otaku.kickassanime.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.lapism.search.R
import com.lapism.search.databinding.MaterialSearchBarBinding
import com.lapism.search.widget.MaterialSearchLayout
import com.lapism.search.widget.MaterialSearchToolbar
import com.lapism.search.widget.NavigationIconCompat


@SuppressLint("PrivateResource")
@Suppress("unused", "MemberVisibilityCanBePrivate")
class MaterialSearchBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : MaterialSearchLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var mCustomMarginsBottom: Int? = null
    private var mCustomMarginsTop: Int? = null
    private var mCustomMarginsEnd: Int? = null
    private var mCustomMarginsStart: Int? = null

    // *********************************************************************************************
    private var binding: MaterialSearchBarBinding
    private var a: TypedArray? = null

    // *********************************************************************************************
    init {
        val inflater = LayoutInflater.from(getContext())
        binding = MaterialSearchBarBinding.inflate(inflater, this)

        a = context.obtainStyledAttributes(
            attrs, R.styleable.MaterialSearchBar, defStyleAttr, defStyleRes
        )

        when {
            a?.hasValue(R.styleable.MaterialSearchBar_search_navigationIconCompat)!! -> {
                navigationIconCompat = a?.getInt(
                    R.styleable.MaterialSearchBar_search_navigationIconCompat,
                    NavigationIconCompat.NONE
                )!!
            }

            a?.hasValue(R.styleable.MaterialSearchBar_search_navigationIcon)!! -> {
                setNavigationIcon(a?.getDrawable(R.styleable.MaterialSearchBar_search_navigationIcon))
            }
        }

        if (a?.hasValue(R.styleable.MaterialSearchBar_search_navigationContentDescription)!!) {
            val description =
                a?.getText(R.styleable.MaterialSearchBar_search_navigationContentDescription)
            setNavigationContentDescription(description)
        }

        if (a?.hasValue(R.styleable.MaterialSearchBar_search_navigationBackgroundColor)!!) {
            val color = a?.getInt(R.styleable.MaterialSearchBar_search_navigationBackgroundColor, 0)
            setNavigationBackgroundColor(color!!)
        }

        if (a?.hasValue(R.styleable.MaterialSearchBar_search_navigationElevation)!!) {
            val navigationElevation =
                a?.getDimensionPixelSize(
                    R.styleable.MaterialSearchBar_search_navigationElevation,
                    0
                )
            setNavigationElevation(navigationElevation?.toFloat()!!)
        }

        if (a?.hasValue(R.styleable.MaterialSearchBar_search_radius)!!) {
            val customRadius =
                a?.getDimensionPixelSize(R.styleable.MaterialSearchBar_search_radius, 0)
            setRadius(customRadius?.toFloat()!!)
        }

        if (a?.hasValue(R.styleable.MaterialSearchBar_android_hint)!!) {
            val hint = a?.getString(R.styleable.MaterialSearchBar_android_hint)
            setHint(hint)
        }

        val defaultMarginsStartEnd =
            context.resources.getDimensionPixelSize(R.dimen.search_dp_16)
        val defaultMarginsTopBottom =
            context.resources.getDimensionPixelSize(R.dimen.search_dp_8)

        mCustomMarginsStart = a?.getDimensionPixelSize(
            R.styleable.MaterialSearchBar_android_layout_marginStart,
            defaultMarginsStartEnd
        )
        mCustomMarginsEnd = a?.getDimensionPixelSize(
            R.styleable.MaterialSearchBar_android_layout_marginEnd,
            defaultMarginsStartEnd
        )
        mCustomMarginsTop = a?.getDimensionPixelSize(
            R.styleable.MaterialSearchBar_android_layout_marginTop,
            defaultMarginsTopBottom
        )
        mCustomMarginsBottom = a?.getDimensionPixelSize(
            R.styleable.MaterialSearchBar_android_layout_marginBottom,
            defaultMarginsTopBottom
        )

        a?.recycle()
    }

    // *********************************************************************************************
    override fun setNavigationIcon(@DrawableRes resId: Int) {
        binding.searchBarToolbar.setNavigationIcon(resId)
    }

    override fun setNavigationIcon(drawable: Drawable?) {
        binding.searchBarToolbar.navigationIcon = drawable
    }

    override fun setNavigationContentDescription(@StringRes resId: Int) {
        binding.searchBarToolbar.setNavigationContentDescription(resId)
    }

    override fun setNavigationContentDescription(description: CharSequence?) {
        binding.searchBarToolbar.navigationContentDescription = description
    }

    override fun setNavigationOnClickListener(listener: OnClickListener) {
        binding.searchBarToolbar.setNavigationOnClickListener(listener)
    }

    override fun setNavigationElevation(elevation: Float) {
        binding.searchBarCard.cardElevation = elevation
    }

    override fun setNavigationBackgroundColor(@ColorInt color: Int) {
        binding.searchBarCard.setCardBackgroundColor(color)
    }

    // *********************************************************************************************
    override fun setOnClickListener(l: OnClickListener?) {
        binding.searchBarToolbar.setOnClickListener(l)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val customMarginsStart = mCustomMarginsStart ?: return
        val customMarginsTop = mCustomMarginsTop ?: return
        val customMarginsEnd = mCustomMarginsEnd ?: return
        val customMarginsBottom = mCustomMarginsBottom ?: return
        setMargins(customMarginsStart, customMarginsTop, customMarginsEnd, customMarginsBottom)
    }

    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState()
        val parcelableSparseIntArray = ParcelableSparseIntArray(4)
        parcelableSparseIntArray.put(0, mCustomMarginsStart ?: 0)
        parcelableSparseIntArray.put(1, mCustomMarginsEnd ?: 0)
        parcelableSparseIntArray.put(2, mCustomMarginsTop ?: 0)
        parcelableSparseIntArray.put(3, mCustomMarginsBottom ?: 0)
        return parcelableSparseIntArray
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is ParcelableSparseIntArray) {
            mCustomMarginsStart = state[0, mCustomMarginsStart ?: 0]
            mCustomMarginsEnd = state[1, mCustomMarginsEnd ?: 0]
            mCustomMarginsTop = state[2, mCustomMarginsTop ?: 0]
            mCustomMarginsBottom = state[3, mCustomMarginsBottom ?: 0]
        }
        super.onRestoreInstanceState(state)
    }

    // *********************************************************************************************
    fun getToolbar(): MaterialSearchToolbar {
        return binding.searchBarToolbar
    }

    fun setText(text: CharSequence?) {
        binding.searchBarToolbar.setText(text)
    }

    fun setHint(hint: CharSequence?) {
        binding.searchBarToolbar.setHint(hint)
    }

    fun setForegroundColor(foregroundColor: ColorStateList?) {
        binding.searchBarCard.setCardForegroundColor(foregroundColor)
    }

    fun setStrokeWidth(@Dimension strokeWidth: Int) {
        binding.searchBarCard.strokeWidth = strokeWidth
    }

    fun setStrokeColor(strokeColor: ColorStateList) {
        binding.searchBarCard.setStrokeColor(strokeColor)
    }

    fun setRadius(radius: Float) {
        binding.searchBarCard.radius = radius
    }

    // *********************************************************************************************
    private fun setMargins(left: Int, top: Int, right: Int, bottom: Int) {
        if (binding.searchBarCard.layoutParams is MarginLayoutParams) {
            val params = binding.searchBarCard.layoutParams as? MarginLayoutParams
            params?.setMargins(left, top, right, bottom)
            binding.searchBarCard.layoutParams = params
        }
    }


}