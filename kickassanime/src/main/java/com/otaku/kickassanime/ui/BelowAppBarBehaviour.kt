package com.otaku.kickassanime.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout

class BelowAppBarBehaviour : CoordinatorLayout.Behavior<SearchBar> {
    constructor() : super()
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: SearchBar,
        dependency: View
    ): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: SearchBar,
        dependency: View
    ): Boolean {
        if (child.top != dependency.bottom && dependency is AppBarLayout) {
            child.layoutParams = (child.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
                topMargin = dependency.bottom
            }
            return true
        }
        return super.onDependentViewChanged(parent, child, dependency)
    }

    override fun onDependentViewRemoved(
        parent: CoordinatorLayout,
        child: SearchBar,
        dependency: View
    ) {
        child.layoutParams = (child.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
            topMargin = 0
        }
        super.onDependentViewRemoved(parent, child, dependency)
    }
}