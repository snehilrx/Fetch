package com.otaku.fetch.base.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class OrientationAwareRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var lastX = 0.0f
    private var lastY = 0.0f
    private var scrolling = false

    init {
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                scrolling = newState != SCROLL_STATE_IDLE
            }
        })
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        val lm = layoutManager ?: return super.onInterceptTouchEvent(e)

        var allowScroll = true

        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = e.x
                lastY = e.y

                if (scrolling) {
                    val newEvent = MotionEvent.obtain(e)
                    newEvent.action = MotionEvent.ACTION_UP

                    stopScroll()

                    return super.onInterceptTouchEvent(newEvent)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val currentX = e.x
                val currentY = e.y
                val dx = abs(currentX - lastX)
                val dy = abs(currentY - lastY)
                allowScroll = if (dy > dx) lm.canScrollVertically() else lm.canScrollHorizontally()
            }
        }

        return if (!allowScroll) {
            false
        } else super.onInterceptTouchEvent(e)
    }

    companion object {
        private val TAG = OrientationAwareRecyclerView::class.java.toString()
    }
}