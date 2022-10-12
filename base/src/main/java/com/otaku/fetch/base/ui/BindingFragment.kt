package com.otaku.fetch.base.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.otaku.fetch.base.databinding.AppbarImageBinding
import com.otaku.fetch.base.databinding.AppbarShineBinding

open class BindingFragment<T : ViewDataBinding>(@LayoutRes private val layoutRes: Int) :
    Fragment() {

    protected lateinit var binding: T

    private var mStatusBarHeight: Int = 0

    private var appbarLayout: AppBarLayout? = null
    private var shineView: ShineView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)
        mStatusBarHeight = getStatusBarHeight()
        return binding.root
    }

    protected fun initAppbar(
        binding: AppbarShineBinding,
        navController: NavController
    ) {
        initAppbar(
            binding.shineView,
            binding.toolbar,
            binding.collapsingToolbar,
            binding.appbarLayout,
            navController
        )
    }

    protected fun initAppbar(
        shineView: ShineView,
        toolbar: Toolbar,
        collapsingToolbar: CollapsingToolbarLayout,
        appbarLayout: AppBarLayout,
        navController: NavController
    ) {
        this.appbarLayout = appbarLayout
        this.shineView = shineView
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        NavigationUI.setupWithNavController(collapsingToolbar, toolbar, navController)
        shineView.statusbarHeight = mStatusBarHeight.toFloat()
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                appbarLayout.removeOnOffsetChangedListener(shineView)
                super.onPause(owner)
            }

            override fun onResume(owner: LifecycleOwner) {
                appbarLayout.addOnOffsetChangedListener(shineView)
                super.onResume(owner)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                appbarLayout.removeOnOffsetChangedListener(shineView)
                super.onDestroy(owner)
            }
        })
        setupToolbar(toolbar)
    }

    protected fun initAppbar(
        binding: AppbarImageBinding,
        navController: NavController
    ) {
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        NavigationUI.setupWithNavController(
            binding.collapsingToolbar,
            binding.toolbar,
            navController
        )
        setupToolbar(binding.toolbar)
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.layoutParams = toolbar.layoutParams?.apply {
            height += mStatusBarHeight
        }
        toolbar.apply {
            setPadding(paddingLeft, paddingTop + mStatusBarHeight, paddingRight, paddingBottom)
        }
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }


    protected fun AppBarLayout.getAppBarBehavior() =
        (layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? AppBarLayout.Behavior

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
        appbarLayout?.removeOnOffsetChangedListener(shineView)
    }
}

/**
 * Set the adapter and call [clearReference] extension function in one call.
 * Use this extension if the current Fragment is going to be REPLACED. (When using fragmentTransaction.add is not necessary) the back stack.
 */
fun <VH : RecyclerView.ViewHolder> RecyclerView.setNullableAdapter(
    adapter: RecyclerView.Adapter<VH>
) {
    this.adapter = adapter
    this.clearReference()
}

/**
 * Remove the adapter after the view has been detached from window in order to prevent memory leaks.
 */
internal fun RecyclerView.clearReference() {
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View?) {

        }

        override fun onViewDetachedFromWindow(v: View?) {
            this@clearReference.adapter = null
        }
    })
}