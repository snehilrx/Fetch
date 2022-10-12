package com.otaku.fetch.base.ui

import android.content.Context
import android.text.*
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.otaku.fetch.base.R
import com.otaku.fetch.base.utils.UiUtils.toPxInt
import com.otaku.fetch.bindings.ImageViewBindings
import kotlin.math.ceil

class MarginTextView : FrameLayout {

    lateinit var imageView: ImageView
    lateinit var textView: MaterialTextView

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr)
    }

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr)
    }


    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        imageView = ShapeableImageView(context, attrs, defStyle)
        textView = MaterialTextView(context, attrs, defStyle)
        imageView.maxHeight = resources.getDimension(R.dimen.item_view_width).toInt()
        imageView.layoutParams = LayoutParams(resources.getDimension(R.dimen.item_view_width).toInt(), LayoutParams.WRAP_CONTENT)
        addView(imageView)
        addView(textView)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        marginTextView()
    }

    private fun marginTextView() {
        val imageViewWidth = imageView.measuredWidth
        val lines = ceil(imageView.measuredHeight / (textView.lineHeight - 1.25*textView.lineSpacingExtra)).toInt()
        val marginText = MyLeadingMarginSpan2(lines, imageViewWidth + 12.toPxInt)
        val spannableStringBuilder = SpannableStringBuilder(textView.text.toString())
        if(textView.lineCount <= lines){
            spannableStringBuilder.setSpan(
                marginText,
                0,
                spannableStringBuilder.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            // find the breakpoint where to break the String.
            val breakpoint = textView.layout.getLineEnd(lines-1);

            spannableStringBuilder.setSpan(MyLeadingMarginSpan2(lines, imageViewWidth + 12.toPxInt), 0, breakpoint, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.text = spannableStringBuilder
    }

    companion object {
        @BindingAdapter("src", "text")
        @JvmStatic
        fun setImage(view: MarginTextView, image: String?, text: String?) {
            ImageViewBindings.imageUrl(view.imageView, image)
            view.textView.text = text
        }
    }
}